package com.tequipy.interview.webhook

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "webhook_events")
class WebhookEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var provider: String,
    var eventId: String,
    var payload: String,

    @Enumerated(EnumType.STRING)
    var state: WebhookState = WebhookState.RECEIVED,

    var attempts: Int = 0,
    var rt: Instant = Instant.now()
)

enum class WebhookState { RECEIVED, PROCESSING, DONE, FAILED }
