package com.tequipy.interview.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class AppMetrics(registry: MeterRegistry) {
    init {
        // track when the service started — useful for uptime monitoring
        registry.gauge("app_started_timestamp", System.currentTimeMillis().toDouble())
    }
}
