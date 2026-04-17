package com.tequipy.interview.retry

import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Decides when a webhook should be retried next.
 *
 * Exponential backoff: the N-th attempt waits 30s * 2^N, capped at 1 hour.
 * The retry scheduler consults this to skip webhooks whose next-retry time
 * has not arrived yet.
 */
@Component
class RetryScheduleCalculator {

    fun nextRetryAt(attempts: Int, lastAttempt: Instant): Instant {
        val delaySeconds = BASE_SECONDS * (1L shl attempts)
        val capped = minOf(delaySeconds, MAX_SECONDS)
        return lastAttempt.plusSeconds(capped)
    }

    companion object {
        private const val BASE_SECONDS = 30L
        private const val MAX_SECONDS = 3600L
    }
}
