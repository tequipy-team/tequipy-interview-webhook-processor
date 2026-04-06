package com.tequipy.interview.order

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "order_status_history")
class OrderStatusHistory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus,

    var changedAt: Instant = Instant.now()
)
