package com.tequipy.interview.webhook

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class StripeSignatureFilterTest {

    private val testSecret = "whsec_test_abc123"
    private val filter = StripeSignatureFilter(testSecret)

    private fun computeHmac(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `valid HMAC signature passes stripe filter`() {
        val body = """{"id":"evt_001","type":"payment.succeeded"}"""
        val validSig = computeHmac(body, testSecret)

        val request = MockHttpServletRequest("POST", "/webhooks/stripe")
        request.addHeader("X-Stripe-Signature", validSig)
        request.setContent(body.toByteArray())

        val response = MockHttpServletResponse()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(any(), any())
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `invalid HMAC signature is rejected with 401`() {
        val body = """{"id":"evt_001","type":"payment.succeeded"}"""

        val request = MockHttpServletRequest("POST", "/webhooks/stripe")
        request.addHeader("X-Stripe-Signature", "tampered_signature")
        request.setContent(body.toByteArray())

        val response = MockHttpServletResponse()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain, never()).doFilter(any(), any())
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `missing signature header is rejected with 401`() {
        val body = """{"id":"evt_001","type":"payment.succeeded"}"""

        val request = MockHttpServletRequest("POST", "/webhooks/stripe")
        request.setContent(body.toByteArray())

        val response = MockHttpServletResponse()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain, never()).doFilter(any(), any())
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `non-stripe request passes through without HMAC check`() {
        val request = MockHttpServletRequest("POST", "/webhooks/trackingmore")
        request.setContent("""{}""".toByteArray())

        val response = MockHttpServletResponse()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(any(), any())
    }
}
