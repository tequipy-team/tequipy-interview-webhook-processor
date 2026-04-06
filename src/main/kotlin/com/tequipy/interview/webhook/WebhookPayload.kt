package com.tequipy.interview.webhook

data class WebhookPayload(
    val id: String?,
    val orderId: String?,
    val st: String?,
    val ts: Long?
)
