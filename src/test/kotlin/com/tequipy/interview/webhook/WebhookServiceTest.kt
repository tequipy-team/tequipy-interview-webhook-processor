package com.tequipy.interview.webhook

import com.tequipy.interview.carrier.CarrierClient
import com.tequipy.interview.carrier.CarrierStatusResponse
import com.tequipy.interview.events.EventPublisher
import com.tequipy.interview.events.OrderStatusChanged
import com.tequipy.interview.order.OrderService
import com.tequipy.interview.retry.RetryPolicy
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Assertions.*

class WebhookServiceTest {

    private val webhookEventRepository = mockk<WebhookEventRepository>()
    private val orderService = mockk<OrderService>()
    private val eventPublisher = mockk<EventPublisher>()
    private val carrierClient = mockk<CarrierClient>()
    private val retryPolicy = mockk<RetryPolicy>()

    private val service = WebhookService(
        webhookEventRepository, orderService, eventPublisher, carrierClient, retryPolicy
    )

    @Test
    fun `processes event and updates order`() {
        val event = WebhookEvent(
            id = 1L,
            provider = "stripe",
            eventId = "evt_001",
            payload = """{"order_id":"ord_42","status":"SHIPPED"}"""
        )

        every { webhookEventRepository.save(any()) } returnsArgument 0
        every { carrierClient.fetchStatus("ord_42") } returns CarrierStatusResponse("SHIPPED")
        every { orderService.updateStatus(any(), any()) } just Runs
        every { eventPublisher.publish(any()) } just Runs
        every { retryPolicy.maxAttempts } returns 10

        service.processEvent(event)

        assertEquals(WebhookState.DONE, event.state)
        verify { orderService.updateStatus("ord_42", any()) }
        verify { eventPublisher.publish(any<OrderStatusChanged>()) }
    }

    @Test
    fun `retries on carrier failure`() {
        val event = WebhookEvent(
            id = 2L,
            provider = "stripe",
            eventId = "evt_002",
            payload = """{"order_id":"ord_43","status":"SHIPPED"}""",
            attempts = 0
        )

        every { webhookEventRepository.save(any()) } returnsArgument 0
        every { carrierClient.fetchStatus(any()) } throws RuntimeException("carrier down")
        every { retryPolicy.maxAttempts } returns 10

        service.processEvent(event)

        assertEquals(1, event.attempts)
        assertEquals(WebhookState.RECEIVED, event.state)
    }

    @Disabled("flaky — fix later")
    @Test
    fun testConcurrentWebhooks() {
        // TODO: stabilise — sometimes passes sometimes doesn't under load
    }
}
