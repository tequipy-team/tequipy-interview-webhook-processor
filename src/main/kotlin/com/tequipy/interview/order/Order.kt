package com.tequipy.interview.order

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var externalId: String,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var statusHistory: MutableList<OrderStatusHistory> = mutableListOf()
)

enum class OrderStatus {
    PENDING, RESERVED, PAID, SHIPPED, DELIVERED, CANCELLED
}
