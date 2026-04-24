package com.myecommerce.automation.dsl.domain

data class CartState(
    val itemCount: Int,
    val total: String,
    val items: List<CartItem>,
    val isEmpty: Boolean,
) {
    val hasItems: Boolean get() = itemCount > 0
}
