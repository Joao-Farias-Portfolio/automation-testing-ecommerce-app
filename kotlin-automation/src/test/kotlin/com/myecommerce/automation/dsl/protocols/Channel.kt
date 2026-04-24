package com.myecommerce.automation.dsl.protocols

enum class Channel {
    WEB, API;

    companion object {
        private var instance: Channel? = null

        fun current(): Channel = instance ?: resolve().also { instance = it }

        private fun resolve(): Channel {
            val raw = System.getProperty("channel")?.trim()
                ?: throw IllegalStateException("System property '-Dchannel' is required. Valid values: Web, API")
            return runCatching { valueOf(raw.uppercase()) }.getOrElse {
                throw IllegalStateException("Unknown channel '$raw'. Valid values: Web, API")
            }
        }
    }
}
