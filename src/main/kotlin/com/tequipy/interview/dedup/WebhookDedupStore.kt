package com.tequipy.interview.dedup

import com.tequipy.interview.webhook.WebhookEvent
import com.tequipy.interview.webhook.WebhookEventRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class WebhookDedupStore(private val webhookEventRepository: WebhookEventRepository) {

    /**
     * Saves the event if it doesn't already exist.
     * Returns true if saved, false if duplicate.
     */
    fun saveIfNotExists(event: WebhookEvent): Boolean {
        return try {
            webhookEventRepository.save(event)
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
    }
}
