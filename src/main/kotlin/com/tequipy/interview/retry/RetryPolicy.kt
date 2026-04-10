package com.tequipy.interview.retry

import org.springframework.stereotype.Component

@Component
class RetryPolicy {

    val maxAttempts = 5

    fun delay(attempt: Int): Long {
        return 1000L * attempt
    }

    fun shouldRetry(attempts: Int): Boolean = attempts < maxAttempts
}
