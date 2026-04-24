package com.myecommerce.automation.driver.api

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiProduct(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    @JsonProperty("image_url") val imageUrl: String?,
)

data class ApiProductDetail(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    @JsonProperty("image_url") val imageUrl: String?,
    @JsonProperty("delivery_options") val deliveryOptions: List<ApiDeliveryOption>,
)

data class ApiDeliveryOption(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    @JsonProperty("is_active") val isActive: Boolean,
    @JsonProperty("min_order_amount") val minOrderAmount: Double?,
)
