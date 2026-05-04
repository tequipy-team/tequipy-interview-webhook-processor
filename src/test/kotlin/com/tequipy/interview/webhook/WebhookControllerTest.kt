package com.tequipy.interview.webhook

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
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
    fun `webhook body is not included in log output`() {
        val sensitivePayload = """{"id":"evt_sec_001","customer_email":"user@example.com","card_last4":"4242","order_id":"ord_1"}"""

        val logger = LoggerFactory.getLogger(WebhookController::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)

        try {
            whenever(webhookEventRepository.save(any<WebhookEvent>())).thenAnswer { it.arguments[0] }
            doNothing().whenever(service).processEvent(any<WebhookEvent>())

            mockMvc.perform(
                post("/webhooks/stripe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sensitivePayload)
            ).andExpect(status().isOk)

            val logMessages = listAppender.list.map { it.formattedMessage }
            logMessages.forEach { message ->
                assertThat(message).doesNotContain("customer_email")
                assertThat(message).doesNotContain("card_last4")
                assertThat(message).doesNotContain("user@example.com")
            }
        } finally {
            logger.detachAppender(listAppender)
        }
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
