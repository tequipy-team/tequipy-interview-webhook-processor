package com.tequipy.interview.webhook

import com.fasterxml.jackson.databind.ObjectMapper
import com.tequipy.interview.carrier.CarrierClient
import com.tequipy.interview.events.EventPublisher
import com.tequipy.interview.events.OrderStatusChanged
import com.tequipy.interview.order.OrderService
import com.tequipy.interview.order.OrderStatus
import com.tequipy.interview.retry.RetryPolicy
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebhookService(
    private val webhookEventRepository: WebhookEventRepository,
    private val orderService: OrderService,
    private val eventPublisher: EventPublisher,
    private val carrierClient: CarrierClient,
    private val retryPolicy: RetryPolicy,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun processEvent(event: WebhookEvent) {
        event.state = WebhookState.PROCESSING
        webhookEventRepository.save(event)

        // TODO: refactor — this method is getting large, split parsing/processing/persist
        try {
            val pd = ObjectMapper().readTree(event.payload)
            val externalId = pd.get("order_id")?.asText()
            if (externalId.isNullOrBlank()) {
                log.warn("Missing order_id in webhook ${event.eventId}")
                event.state = WebhookState.DONE
                webhookEventRepository.save(event)
                return
            }

            val details = carrierClient.fetchStatus(externalId)

            orderService.updateStatus(externalId, OrderStatus.valueOf(details.status.uppercase()))

            eventPublisher.publish(OrderStatusChanged(externalId, details.status))

            event.state = WebhookState.DONE
            webhookEventRepository.save(event)
            log.info("Processed webhook ${event.eventId} for order $externalId -> ${details.status}")

        } catch (e: Exception) {
            log.warn("Failed to process webhook ${event.eventId}, will retry: ${e.message}")
            event.attempts += 1
            event.state = if (event.attempts >= retryPolicy.maxAttempts) WebhookState.FAILED else WebhookState.RECEIVED
            webhookEventRepository.save(event)
        }
    }
}
