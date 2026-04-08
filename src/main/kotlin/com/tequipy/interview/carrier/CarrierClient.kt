package com.tequipy.interview.carrier

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class CarrierClient {

    private val restTemplate = RestTemplate()
    private val baseUrl = "https://api.carrier.example.com"

    fun fetchStatus(orderId: String): CarrierStatusResponse {
        return restTemplate.getForObject(
            "$baseUrl/orders/$orderId/status",
            CarrierStatusResponse::class.java
        ) ?: throw RuntimeException("Carrier returned null response for order $orderId")
    }
}

data class CarrierStatusResponse(
    val status: String,
    val carrier: String? = null
)
