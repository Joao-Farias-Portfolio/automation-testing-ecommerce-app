package com.myecommerce.automation.dsl.domain

data class SavedState(
    val saveButtonPresent: Boolean,
    val saveButtonPressed: Boolean,
    val saveButtonEnabled: Boolean,
    val savedPageCount: Int,
    val wishlistLinkVisible: Boolean,
)
