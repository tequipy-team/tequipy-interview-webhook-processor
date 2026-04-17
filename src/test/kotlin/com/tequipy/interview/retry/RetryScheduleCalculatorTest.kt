package com.tequipy.interview.retry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant

class RetryScheduleCalculatorTest {

    private val calculator = RetryScheduleCalculator()
    private val anchor = Instant.parse("2026-04-20T10:00:00Z")

    @Test
    fun `first retry waits 30 seconds`() {
        val next = calculator.nextRetryAt(attempts = 0, lastAttempt = anchor)
        assertEquals(anchor.plusSeconds(30), next)
    }

    @Test
    fun `backoff is exponential for small attempts`() {
        assertEquals(anchor.plusSeconds(60),  calculator.nextRetryAt(1, anchor))
        assertEquals(anchor.plusSeconds(120), calculator.nextRetryAt(2, anchor))
        assertEquals(anchor.plusSeconds(240), calculator.nextRetryAt(3, anchor))
    }

    @Test
    fun `backoff is capped at 1 hour`() {
        val next = calculator.nextRetryAt(attempts = 10, lastAttempt = anchor)
        assertEquals(anchor.plusSeconds(3600), next)
    }

    @Disabled("prod incident INC-8821 — retries fire faster than expected for events stuck in high-attempt loops. TODO: investigate.")
    @Test
    fun `backoff never collapses below the cap for very high attempts`() {
        val cap = anchor.plusSeconds(3600)
        for (attempts in 20..100) {
            val next = calculator.nextRetryAt(attempts, anchor)
            assertTrue(
                !next.isBefore(cap),
                "next retry should be >= cap for attempts=$attempts, got $next (anchor=$anchor)"
            )
        }
    }
}
