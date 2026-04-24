package com.myecommerce.automation.dsl.domain

data class ProductListing(
    val cards: List<ProductCard>,
    val hasVisibleLoadingIndicators: Boolean,
)
