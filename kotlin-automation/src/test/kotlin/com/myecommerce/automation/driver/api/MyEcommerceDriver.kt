package com.myecommerce.automation.driver.api

import com.myecommerce.automation.driver.ports.HttpPort
import com.myecommerce.automation.driver.ports.getAs
import com.myecommerce.automation.driver.ports.getListAs
import com.myecommerce.automation.driver.ports.getListWithQueryAs
import com.myecommerce.automation.dsl.domain.DeliveryOption
import com.myecommerce.automation.dsl.domain.DeliveryState
import com.myecommerce.automation.dsl.domain.ProductCard
import com.myecommerce.automation.dsl.domain.ProductDetail
import com.myecommerce.automation.dsl.domain.ProductListing
import com.myecommerce.automation.dsl.domain.SearchResults
import com.myecommerce.automation.dsl.protocols.CatalogueProtocol
import com.myecommerce.automation.dsl.protocols.Channel
import com.myecommerce.automation.dsl.protocols.DriverRegistry

class MyEcommerceDriver(private val http: HttpPort) : CatalogueProtocol {

    private var currentProductId: Int = -1
    private var lastSearchTerm: String = ""

    companion object {
        private const val BASE_URL = "http://localhost:8001"

        init {
            DriverRegistry.register(Channel.API) {
                val http: HttpPort = if ("okhttp" == System.getProperty("http.impl", "restassured"))
                    OkHttpHttpPort(BASE_URL)
                else
                    RestAssuredHttpPort(BASE_URL)
                MyEcommerceDriver(http)
            }
        }
    }

    override fun browseCatalogue() {}
    override fun returnToProductListing() {}
    override fun chooseAlternativeDeliveryOption() {}

    override fun viewFirstProduct() {
        currentProductId = fetchProducts("").first().id
    }

    override fun searchFor(term: String) {
        lastSearchTerm = term
    }

    override fun getProductListing(): ProductListing {
        val cards = fetchProducts("").map { it.toProductCard() }
        return ProductListing(cards, hasVisibleLoadingIndicators = false)
    }

    override fun getProductDetail(): ProductDetail {
        val product = fetchProductDetail(currentProductId)
        return ProductDetail(
            title = product.title,
            price = formatPrice(product.price),
            description = product.description,
            imagePresent = !product.imageUrl.isNullOrBlank(),
            addToCartButtonText = "Add to Cart",
            addToCartEnabled = true,
        )
    }

    override fun getDeliveryState(): DeliveryState {
        val activeOptions = fetchActiveDeliveryOptions()
        if (activeOptions.isEmpty()) {
            return DeliveryState(
                sectionVisible = false,
                options = emptyList(),
                headerText = "",
                minimumOrderTextPresent = false,
            )
        }
        return DeliveryState(
            sectionVisible = true,
            options = activeOptions.mapIndexed { i, opt -> DeliveryOption(opt.name, i == 0) },
            headerText = "Delivery Options",
            minimumOrderTextPresent = false,
        )
    }

    override fun getSearchResults(): SearchResults {
        val cards = fetchProducts(lastSearchTerm).map { it.toProductCard() }
        return SearchResults(cards, emptyStateVisible = cards.isEmpty())
    }

    override fun currentUrl(): String = when {
        lastSearchTerm.isNotBlank() -> "$BASE_URL/products?search=$lastSearchTerm"
        currentProductId >= 0      -> "$BASE_URL/products/$currentProductId"
        else                       -> "$BASE_URL/products"
    }

    private fun fetchProducts(searchTerm: String): List<ApiProduct> =
        if (searchTerm.isBlank())
            http.getListAs<ApiProduct>("/products")
        else
            http.getListWithQueryAs("/products", "search", searchTerm)

    private fun fetchProductDetail(id: Int): ApiProductDetail = http.getAs("/products/$id")

    private fun fetchActiveDeliveryOptions(): List<ApiDeliveryOption> =
        fetchProductDetail(currentProductId).deliveryOptions.filter { it.isActive }

    private fun ApiProduct.toProductCard() = ProductCard(
        title = title,
        price = formatPrice(price),
        imageUrl = imageUrl?.let { if (it.startsWith("http")) it else "$BASE_URL$it" } ?: "",
    )

    private fun formatPrice(price: Double) = "$${"%.2f".format(price)}"
}
