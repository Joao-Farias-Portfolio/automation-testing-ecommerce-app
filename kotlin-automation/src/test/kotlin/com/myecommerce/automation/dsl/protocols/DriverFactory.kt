package com.myecommerce.automation.dsl.protocols

fun createCatalogue(): CatalogueProtocol = DriverRegistry.create(Channel.current())

fun createCart(): CartProtocol {
    val driver = DriverRegistry.create(Channel.current())
    return driver as? CartProtocol
        ?: throw IllegalStateException("Channel '${Channel.current()}' does not support cart operations")
}

fun createSaved(): SavedProtocol {
    val driver = DriverRegistry.create(Channel.current())
    return driver as? SavedProtocol
        ?: throw IllegalStateException("Channel '${Channel.current()}' does not support saved/wishlist operations")
}
