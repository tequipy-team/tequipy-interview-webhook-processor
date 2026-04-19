package com.tequipy.interview.webhook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class StripeSignatureFilterTest {

    private val testSecret = "test-stripe-secret"
    private val filter = StripeSignatureFilter(testSecret)

    private fun computeHmac(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `valid signature passes through`() {
        val body = """{"id":"evt_001","type":"charge.succeeded"}"""
        val sig = computeHmac(body, testSecret)

        val request = MockHttpServletRequest("POST", "/webhooks/stripe")
        request.setContent(body.toByteArray())
        request.addHeader("X-Stripe-Signature", sig)

        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
    }

    @Test
    fun `invalid signature returns 401`() {
        val body = """{"id":"evt_002","type":"charge.failed"}"""

        val request = MockHttpServletRequest("POST", "/webhooks/stripe")
        request.setContent(body.toByteArray())
        request.addHeader("X-Stripe-Signature", "wrong-signature")

        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(401, response.status)
    }

    @Test
    fun `non-stripe path skips filter`() {
        val request = MockHttpServletRequest("POST", "/webhooks/trackingmore")
        request.setContent("""{"id":"evt_003"}""".toByteArray())

        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
    }
}
