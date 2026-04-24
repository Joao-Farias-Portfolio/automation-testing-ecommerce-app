package com.myecommerce.automation.dsl.protocols

import com.myecommerce.automation.dsl.domain.DeliveryState
import com.myecommerce.automation.dsl.domain.ProductDetail
import com.myecommerce.automation.dsl.domain.ProductListing
import com.myecommerce.automation.dsl.domain.SearchResults

interface CatalogueProtocol {
    fun browseCatalogue()
    fun viewFirstProduct()
    fun searchFor(term: String)
    fun returnToProductListing()
    fun chooseAlternativeDeliveryOption()

    fun getProductListing(): ProductListing
    fun getProductDetail(): ProductDetail
    fun getDeliveryState(): DeliveryState
    fun getSearchResults(): SearchResults
    fun currentUrl(): String
}
