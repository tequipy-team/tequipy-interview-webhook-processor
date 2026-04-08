package com.tequipy.interview.order

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OrderService(private val orderRepository: OrderRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun updateStatus(externalId: String, newStatus: OrderStatus) {
        val order = orderRepository.findByExternalId(externalId) ?: run {
            log.warn("Order not found for externalId=$externalId, creating new")
            Order(externalId = externalId).also { orderRepository.save(it) }
        }
        order.status = newStatus
        order.updatedAt = Instant.now()
        orderRepository.save(order)
    }
}
