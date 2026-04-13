package com.tequipy.interview.retry

import com.tequipy.interview.webhook.WebhookService
import com.tequipy.interview.webhook.WebhookEventRepository
import com.tequipy.interview.webhook.WebhookState
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class BatchReplayJob(
    private val webhookEventRepository: WebhookEventRepository,
    private val webhookService: WebhookService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun replayFailed() {
        val since = Instant.now().minus(1, ChronoUnit.HOURS)
        val failed = webhookEventRepository.findByState(WebhookState.FAILED)
            .filter { it.rt.isAfter(since) }

        log.info("BatchReplayJob: replaying ${failed.size} failed webhooks from last hour")

        failed.forEach { event ->
            event.attempts = 0
            webhookEventRepository.save(event)
            webhookService.processEvent(event)
        }
    }
}
