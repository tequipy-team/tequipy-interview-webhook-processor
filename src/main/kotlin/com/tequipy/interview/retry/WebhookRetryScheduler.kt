package com.tequipy.interview.retry

import com.tequipy.interview.webhook.WebhookService
import com.tequipy.interview.webhook.WebhookEventRepository
import com.tequipy.interview.webhook.WebhookState
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WebhookRetryScheduler(
    private val webhookEventRepository: WebhookEventRepository,
    private val webhookService: WebhookService,
    private val retryPolicy: RetryPolicy,
    private val retryScheduleCalculator: RetryScheduleCalculator,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // TODO: refactor — extract candidate selection & policy decisions into a policy component
    @Scheduled(fixedDelay = 30_000)
    fun retryFailed() {
        val now = Instant.now()
        val candidates = webhookEventRepository.findByStateAndAttemptsGreaterThan(
            WebhookState.RECEIVED, 0
        )

        log.info("Retry scheduler: found ${candidates.size} webhooks to retry")

        candidates.forEach { event ->
            val nextRetryAt = retryScheduleCalculator.nextRetryAt(event.attempts, event.rt)
            if (now.isBefore(nextRetryAt)) return@forEach
            if (retryPolicy.shouldRetry(event.attempts)) {
                try {
                    webhookService.processEvent(event)
                } catch (e: Exception) {
                    log.warn("Retry failed for webhook ${event.eventId}: ${e.message}")
                }
            }
        }
    }
}
