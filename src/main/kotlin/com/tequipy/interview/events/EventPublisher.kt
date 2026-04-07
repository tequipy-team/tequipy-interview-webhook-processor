package com.tequipy.interview.events

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class EventPublisher(private val publisher: ApplicationEventPublisher) {

    fun publish(event: OrderStatusChanged) {
        publisher.publishEvent(event)
    }
}
