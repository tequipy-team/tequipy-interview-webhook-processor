package com.tequipy.interview.order

import com.tequipy.interview.events.OrderStatusChanged
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderStatusHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onStatusChanged(event: OrderStatusChanged) {
        log.info("Order status changed: orderId=${event.orderId} newStatus=${event.newStatus}")
        // TODO: propagate to downstream consumers
    }
}
