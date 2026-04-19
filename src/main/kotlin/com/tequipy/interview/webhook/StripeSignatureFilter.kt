package com.tequipy.interview.webhook

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
@Order(1)
class StripeSignatureFilter(
    @Value("\${webhook.stripe.secret}") private val secret: String,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.contains("/stripe")) {
            filterChain.doFilter(request, response)
            return
        }

        val cachedRequest = CachedBodyRequestWrapper(request)
        val signature = request.getHeader("X-Stripe-Signature") ?: ""
        val body = String(cachedRequest.cachedBody)

        val expected = computeHmac(body, secret)

        if (!MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), signature.toByteArray(Charsets.UTF_8))) {
            log.warn("Invalid Stripe signature for request to ${request.requestURI}")
            sendUnauthorized(response, "Invalid signature")
            return
        }

        filterChain.doFilter(cachedRequest, response)
    }

    private fun computeHmac(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun sendUnauthorized(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"error":"$message"}""")
    }

    class CachedBodyRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
        val cachedBody: ByteArray

        init {
            cachedBody = request.inputStream.readBytes()
        }

        override fun getInputStream() = object : jakarta.servlet.ServletInputStream() {
            private val stream = ByteArrayInputStream(cachedBody)
            override fun read() = stream.read()
            override fun isFinished() = stream.available() == 0
            override fun isReady() = true
            override fun setReadListener(listener: jakarta.servlet.ReadListener?) {}
        }

        override fun getReader() = BufferedReader(InputStreamReader(getInputStream()))
    }
}
