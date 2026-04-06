package com.tequipy.interview.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByExternalId(externalId: String): Order?

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.statusHistory WHERE o.id = :id")
    fun findByIdWithStatusHistory(@Param("id") id: Long): Order?
}
