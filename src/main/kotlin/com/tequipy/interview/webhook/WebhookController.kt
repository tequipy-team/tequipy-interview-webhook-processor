package com.tequipy.interview.webhook

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/webhooks")
class WebhookController(
    private val webhookEventRepository: WebhookEventRepository,
    private val service: WebhookService,
    private val inFlightRegistry: WebhookInFlightRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper()

    @PostMapping("/{provider}")
    fun handle(
        @PathVariable provider: String,
        @RequestBody body: String,
        @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<String> {
        log.info("Received webhook from $provider: $body")

        val eventId = extractEventId(body)
        val wh = WebhookEvent(provider = provider, eventId = eventId, payload = body)
        webhookEventRepository.save(wh)

        inFlightRegistry.put(eventId, wh)

        service.processEvent(wh)

        inFlightRegistry.remove(eventId)

        return ResponseEntity.ok("""{"status":"accepted"}""")
    }

    private fun extractEventId(body: String): String {
        return try {
            mapper.readTree(body).get("id")?.asText() ?: ""
        } catch (e: Exception) {
            // REMOVE BEFORE PROD — temporary debug log
            log.warn("Could not parse event id from body, using empty: $body")
            ""
        }
    }
}
