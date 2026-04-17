package com.tequipy.interview.retry

import com.tequipy.interview.webhook.WebhookEventRepository
import com.tequipy.interview.webhook.WebhookState
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Finds webhooks stuck in PROCESSING state (worker crashed mid-flight) and
 * resets them to RECEIVED so the retry scheduler picks them up again.
 *
 * Without this reaper, a worker crash would orphan the record forever.
 */
@Component
class StuckWebhookReaper(
    private val webhookEventRepository: WebhookEventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 60_000)
    fun reapStuck() {
        val threshold = Instant.now().plus(Duration.ofMinutes(5))
        val stuck = webhookEventRepository.findByStateAndRtBefore(WebhookState.PROCESSING, threshold)
        log.info("Reaper: found ${stuck.size} stuck webhooks")
        stuck.forEach { event ->
            log.info("Reaping stuck webhook ${event.eventId}")
            event.state = WebhookState.RECEIVED
            event.attempts = 0
            webhookEventRepository.save(event)
        }
    }
}
