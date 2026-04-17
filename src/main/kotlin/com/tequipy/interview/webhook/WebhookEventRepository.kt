package com.tequipy.interview.webhook

import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventRepository : JpaRepository<WebhookEvent, Long> {
    fun findByStateAndAttemptsGreaterThan(state: WebhookState, attempts: Int): List<WebhookEvent>
    fun findByState(state: WebhookState): List<WebhookEvent>
    fun findByStateAndRtBefore(state: WebhookState, rt: Instant): List<WebhookEvent>
}
