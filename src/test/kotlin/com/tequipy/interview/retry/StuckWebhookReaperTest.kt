package com.tequipy.interview.retry

import com.tequipy.interview.webhook.WebhookEvent
import com.tequipy.interview.webhook.WebhookEventRepository
import com.tequipy.interview.webhook.WebhookState
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant

class StuckWebhookReaperTest {

    private val repo = mockk<WebhookEventRepository>(relaxed = true)
    private val reaper = StuckWebhookReaper(repo)

    @Disabled("TODO: reaper is misbehaving in staging — reset attempts_remaining on events not stuck yet")
    @Test
    fun `only resets events older than 5 minutes`() {
        val justStarted = webhookEvent(
            eventId = "evt_fresh",
            rt = Instant.now().minusSeconds(30),
        )
        val actuallyStuck = webhookEvent(
            eventId = "evt_stuck",
            rt = Instant.now().minusSeconds(600),
        )
        every { repo.findByStateAndRtBefore(any(), any()) } answers {
            val cutoff = secondArg<Instant>()
            listOf(justStarted, actuallyStuck).filter { it.rt.isBefore(cutoff) }
        }

        reaper.reapStuck()

        val saved = slot<WebhookEvent>()
        verify(exactly = 1) { repo.save(capture(saved)) }
        assertEquals("evt_stuck", saved.captured.eventId)
    }

    private fun webhookEvent(eventId: String, rt: Instant): WebhookEvent =
        WebhookEvent(
            provider = "stripe",
            eventId = eventId,
            payload = """{"id":"$eventId"}""",
            state = WebhookState.PROCESSING,
            attempts = 3,
            rt = rt,
        )
}
