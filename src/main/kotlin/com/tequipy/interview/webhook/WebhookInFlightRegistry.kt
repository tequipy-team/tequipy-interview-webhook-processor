package com.tequipy.interview.webhook

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class WebhookInFlightRegistry {

    private val inFlight = ConcurrentHashMap<String, WebhookEvent>()

    fun put(eventId: String, event: WebhookEvent) {
        inFlight[eventId] = event
    }

    fun remove(eventId: String) {
        inFlight.remove(eventId)
    }

    fun size(): Int = inFlight.size

    fun isInFlight(eventId: String): Boolean = inFlight.containsKey(eventId)
}
