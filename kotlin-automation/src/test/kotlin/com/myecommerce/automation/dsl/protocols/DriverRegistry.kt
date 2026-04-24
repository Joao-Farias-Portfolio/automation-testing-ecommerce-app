package com.myecommerce.automation.dsl.protocols

object DriverRegistry {

    private val registry = mutableMapOf<Channel, () -> CatalogueProtocol>()

    fun register(channel: Channel, factory: () -> CatalogueProtocol) {
        registry[channel] = factory
    }

    fun create(channel: Channel): CatalogueProtocol {
        registry[channel]?.let { return it() }
        loadByConvention(channel)
        return registry[channel]?.invoke()
            ?: throw IllegalStateException(
                "No driver registered for channel '$channel'. Valid values: ${registry.keys}"
            )
    }

    private fun loadByConvention(channel: Channel) {
        val className = "com.myecommerce.automation.driver.${channel.name.lowercase()}.MyEcommerceDriver"
        runCatching { Class.forName(className) }
    }
}
