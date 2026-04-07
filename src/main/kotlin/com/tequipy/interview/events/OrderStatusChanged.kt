package com.tequipy.interview.events

import java.time.Instant

data class OrderStatusChanged(
    val orderId: String,
    val newStatus: String,
    val ts: Instant = Instant.now()
)
