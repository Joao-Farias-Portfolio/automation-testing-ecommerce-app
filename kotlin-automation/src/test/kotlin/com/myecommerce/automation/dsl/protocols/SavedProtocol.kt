package com.myecommerce.automation.dsl.protocols

import com.myecommerce.automation.dsl.domain.SavedState

interface SavedProtocol : CatalogueProtocol {
    fun viewSavedItems()
    fun ensureFirstProductIsSaved()
    fun toggleSaveStateOfFirstProduct()
    fun viewWishlist()

    fun getSavedState(): SavedState
}
