package com.tequipy.interview.webhook

import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventRepository : JpaRepository<WebhookEvent, Long> {
    fun findByStateAndAttemptsGreaterThan(state: WebhookState, attempts: Int): List<WebhookEvent>
    fun findByState(state: WebhookState): List<WebhookEvent>
}
