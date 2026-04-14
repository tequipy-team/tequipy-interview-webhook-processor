package com.tequipy.interview.config

import org.springframework.context.annotation.Configuration

@Configuration
class WebhookConfig {
    companion object {
        const val REL_TYPE = 1
        const val MAX_PAYLOAD_SIZE = 65536
    }

    // TODO: refactor — move these to application.yml once we finalise the config surface
    @Deprecated("use REL_TYPE constant instead", ReplaceWith("REL_TYPE"))
    fun legacyRelType(): Int = 1
}
