package com.myecommerce.automation.dsl.domain

data class SearchResults(
    val cards: List<ProductCard>,
    val emptyStateVisible: Boolean,
)
