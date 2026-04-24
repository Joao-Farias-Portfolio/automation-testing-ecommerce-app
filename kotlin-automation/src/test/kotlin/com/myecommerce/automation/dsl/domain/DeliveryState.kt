package com.myecommerce.automation.dsl.domain

data class DeliveryState(
    val sectionVisible: Boolean,
    val options: List<DeliveryOption>,
    val headerText: String,
    val minimumOrderTextPresent: Boolean,
) {
    fun selectedOptionCount(): Long = options.count { it.selected }.toLong()
}
