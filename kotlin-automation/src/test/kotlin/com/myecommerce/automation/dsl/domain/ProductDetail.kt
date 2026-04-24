package com.myecommerce.automation.dsl.domain

data class ProductDetail(
    val title: String,
    val price: String,
    val description: String,
    val imagePresent: Boolean,
    val addToCartButtonText: String,
    val addToCartEnabled: Boolean,
)
