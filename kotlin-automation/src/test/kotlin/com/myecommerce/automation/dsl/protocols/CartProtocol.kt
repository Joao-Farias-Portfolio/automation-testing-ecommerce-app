package com.myecommerce.automation.dsl.protocols

import com.myecommerce.automation.dsl.domain.CartState

interface CartProtocol : CatalogueProtocol {
    fun viewCart()
    fun addProductToCart()
    fun removeFirstItemFromCart()
    fun changeQuantityTo(quantity: Int)

    fun getCartState(): CartState
}
