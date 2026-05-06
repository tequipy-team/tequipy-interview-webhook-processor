package com.tequipy.interview.webhook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SignatureFilterTest {

    private val stripeFilter = StripeSignatureFilter()
    private val trackingMoreFilter = TrackingMoreSignatureFilter()

    @Test
    fun `stripe valid signature passes filter`() {
        val body = """{"id":"evt_001"}"""
        val request = MockHttpServletRequest().apply {
            requestURI = "/webhooks/stripe"
            setContent(body.toByteArray())
            addHeader("X-Stripe-Signature", hmac(body, "whsec_test_abc123"))
        }
        val chain = MockFilterChain()

        stripeFilter.doFilter(request, MockHttpServletResponse(), chain)

        assertNotNull(chain.request)
    }

    @Test
    fun `stripe invalid signature returns 401`() {
        val body = """{"id":"evt_001"}"""
        val request = MockHttpServletRequest().apply {
            requestURI = "/webhooks/stripe"
            setContent(body.toByteArray())
            addHeader("X-Stripe-Signature", "invalid-sig")
        }
        val response = MockHttpServletResponse()

        stripeFilter.doFilter(request, response, MockFilterChain())

        assertEquals(401, response.status)
    }

    @Test
    fun `trackingmore valid signature passes filter`() {
        val body = """{"id":"tm_001"}"""
        val request = MockHttpServletRequest().apply {
            requestURI = "/webhooks/trackingmore"
            setContent(body.toByteArray())
            addHeader("X-TrackingMore-Hmac-SHA256", hmac(body, "tm_test_xyz789"))
        }
        val chain = MockFilterChain()

        trackingMoreFilter.doFilter(request, MockHttpServletResponse(), chain)

        assertNotNull(chain.request)
    }

    @Test
    fun `trackingmore invalid signature returns 401`() {
        val body = """{"id":"tm_001"}"""
        val request = MockHttpServletRequest().apply {
            requestURI = "/webhooks/trackingmore"
            setContent(body.toByteArray())
            addHeader("X-TrackingMore-Hmac-SHA256", "bad-sig")
        }
        val response = MockHttpServletResponse()

        trackingMoreFilter.doFilter(request, response, MockFilterChain())

        assertEquals(401, response.status)
    }

    private fun hmac(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
