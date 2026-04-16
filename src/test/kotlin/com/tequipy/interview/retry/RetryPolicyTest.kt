package com.tequipy.interview.retry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RetryPolicyTest {

    private val retryPolicy = RetryPolicy()

    @Test
    fun `delay grows with attempts`() {
        assertTrue(retryPolicy.delay(3) > retryPolicy.delay(1))
        assertTrue(retryPolicy.delay(5) > retryPolicy.delay(3))
    }
}
