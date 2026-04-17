package com.tequipy.interview.webhook

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(WebhookController::class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var webhookEventRepository: WebhookEventRepository

    @MockBean
    lateinit var service: WebhookService

    @MockBean
    lateinit var inFlightRegistry: WebhookInFlightRegistry

    private val mapper = ObjectMapper()

    @Test
    fun `happy path - webhook processed returns 200`() {
        val payload = """{"id":"evt_001","order_id":"ord_42","status":"SHIPPED"}"""

        whenever(webhookEventRepository.save(any<WebhookEvent>())).thenAnswer { it.arguments[0] }
        doNothing().whenever(service).processEvent(any<WebhookEvent>())

        mockMvc.perform(
            post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Stripe-Signature", "test-sig")
                .content(payload)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `duplicate webhook id returns 200 without error`() {
        val payload = """{"id":"evt_dup_001","order_id":"ord_99","status":"DELIVERED"}"""

        whenever(webhookEventRepository.save(any<WebhookEvent>())).thenAnswer { it.arguments[0] }
        doNothing().whenever(service).processEvent(any<WebhookEvent>())

        mockMvc.perform(
            post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Stripe-Signature", "test-sig")
                .content(payload)
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Stripe-Signature", "test-sig")
                .content(payload)
        ).andExpect(status().isOk)
    }
}
