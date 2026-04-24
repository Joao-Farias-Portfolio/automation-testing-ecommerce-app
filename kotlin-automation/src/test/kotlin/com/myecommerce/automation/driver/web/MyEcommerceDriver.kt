package com.myecommerce.automation.driver.web

import com.myecommerce.automation.driver.ports.BrowserPort
import com.myecommerce.automation.dsl.domain.CartItem
import com.myecommerce.automation.dsl.domain.CartState
import com.myecommerce.automation.dsl.domain.DeliveryOption
import com.myecommerce.automation.dsl.domain.DeliveryState
import com.myecommerce.automation.dsl.domain.ProductCard
import com.myecommerce.automation.dsl.domain.ProductDetail
import com.myecommerce.automation.dsl.domain.ProductListing
import com.myecommerce.automation.dsl.domain.SavedState
import com.myecommerce.automation.dsl.domain.SearchResults
import com.myecommerce.automation.dsl.protocols.Channel
import com.myecommerce.automation.dsl.protocols.DriverRegistry
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol

class MyEcommerceDriver(private val browser: BrowserPort) : MyEcommerceProtocol {

    companion object {
        private const val BASE_URL = "http://localhost:3001"

        private val DELIVERY_SELECTOR =
            "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
            "[data-testid='shipping-section'], [data-testid='shipping-options']"

        init {
            DriverRegistry.register(Channel.WEB) {
                val browser: BrowserPort = SeleniumBrowserPort()
                MyEcommerceDriver(browser)
            }
        }
    }

    override fun browseCatalogue() = browser.navigateTo(BASE_URL)

    override fun viewCart() {
        browser.navigateTo("$BASE_URL/cart")
        browser.waitUntilAnyPresent("[data-testid='cart-item']", "[data-testid='empty-cart']")
    }

    override fun viewSavedItems() {
        browser.navigateTo("$BASE_URL/saved")
        browser.waitUntilUrlContains("/saved")
    }

    override fun returnToProductListing() = browser.navigateBack()

    override fun addProductToCart() {
        browser.waitUntilCountMoreThan("[data-testid='add-to-cart']", 0)
        val countBefore = readCartCount()
        val buttonCount = browser.count("[data-testid='add-to-cart']")
        val enabledIndex = (0 until buttonCount).firstOrNull { browser.isNthEnabled("[data-testid='add-to-cart']", it) } ?: 0
        browser.clickNth("[data-testid='add-to-cart']", enabledIndex)
        browser.waitUntilCondition({ readCartCount() > countBefore }, 10)
    }

    override fun removeFirstItemFromCart() {
        browser.waitUntilCountMoreThan("[data-testid='remove-item']", 0)
        val countBefore = readCartCount()
        browser.clickNth("[data-testid='remove-item']", 0)
        browser.waitUntilCondition(
            { readCartCount() < countBefore || browser.isPresent("[data-testid='empty-cart']") }, 10
        )
    }

    override fun changeQuantityTo(quantity: Int) {
        browser.waitUntilPresent("[data-testid='quantity-display']")
        val totalBefore = readCartTotal()
        browser.setReactInputValue("[data-testid='quantity-display']", quantity.toString())
        browser.waitUntilCondition({ readCartTotal() != totalBefore }, 10)
    }

    override fun searchFor(term: String) {
        browser.sendKeys("input[placeholder*='Search Items']", term, true)
        browser.waitUntilUrlContains("/search/")
    }

    override fun viewFirstProduct() {
        browser.waitUntilVisible("[data-testid='product-card']")
        browser.clickNth("[data-testid='product-card']", 0)
        browser.waitUntilUrlMatches(".*/products/\\d+")
    }

    override fun chooseAlternativeDeliveryOption() {
        val radioCount = browser.count("input[type='radio']")
        for (i in 0 until radioCount) {
            if (!browser.isNthSelected("input[type='radio']", i)) {
                browser.clickXpath("(//input[@type='radio'])[${i + 1}]/../../..")
                return
            }
        }
    }

    override fun ensureFirstProductIsSaved() {
        browser.waitUntilVisible("[data-testid='save-button']")
        val pressed = browser.attribute("[data-testid='save-button']", "aria-pressed").toBoolean()
        if (!pressed) toggleSaveStateOfFirstProduct()
    }

    override fun toggleSaveStateOfFirstProduct() {
        val previousState = browser.attribute("[data-testid='save-button']", "aria-pressed")
        browser.clickNth("[data-testid='save-button']", 0)
        browser.waitUntilAttributeChanges("[data-testid='save-button']", 0, "aria-pressed", previousState)
    }

    override fun viewWishlist() {
        browser.click("[data-testid='wishlist-link']")
        browser.waitUntilUrlContains("/saved")
    }

    override fun getProductListing(): ProductListing {
        browser.waitUntilVisible("[data-testid='product-card']")
        val cards = extractProductCards()
        val loadingVisible = browser.isPresent("[data-testid='loading']") && browser.isVisible("[data-testid='loading']")
        return ProductListing(cards, loadingVisible)
    }

    override fun getCartState(): CartState {
        val count = readCartCount()
        val total = readCartTotal()
        val items = readCartItems()
        val empty = browser.isPresent("[data-testid='empty-cart']")
        return CartState(count, total, items, empty)
    }

    override fun getProductDetail(): ProductDetail {
        browser.waitUntilVisible("[data-testid='product-title']")
        return ProductDetail(
            title = browser.text("[data-testid='product-title']"),
            price = browser.text("[data-testid='product-price']"),
            description = browser.text("[data-testid='product-description']"),
            imagePresent = browser.isPresent("[data-testid='product-detail-image']"),
            addToCartButtonText = browser.text("[data-testid='add-to-cart']"),
            addToCartEnabled = browser.isEnabled("[data-testid='add-to-cart']"),
        )
    }

    override fun getDeliveryState(): DeliveryState {
        browser.waitUntilVisible("[data-testid='product-title']")
        val found = browser.tryWaitUntilPresent(DELIVERY_SELECTOR, 5)
        if (!found || !browser.isVisible(DELIVERY_SELECTOR)) {
            return DeliveryState(sectionVisible = false, options = emptyList(), headerText = "", minimumOrderTextPresent = false)
        }
        val radioCount = browser.count("input[type='radio']")
        val options = (0 until radioCount).map { i ->
            val id = browser.nthAttribute("input[type='radio']", i, "value")
            DeliveryOption(if (id.isBlank()) i.toString() else id, browser.isNthSelected("input[type='radio']", i))
        }
        val header = browser.nthText("$DELIVERY_SELECTOR p", 0)
        val minimumOrderPresent = browser.isPresent("[data-testid='minimum-order'], [data-testid='min-order']")
        return DeliveryState(sectionVisible = true, options = options, headerText = header, minimumOrderTextPresent = minimumOrderPresent)
    }

    override fun getSearchResults(): SearchResults {
        browser.waitUntilUrlContains("/search/")
        val cards = extractProductCards()
        return SearchResults(cards, emptyStateVisible = browser.isPresent("[data-testid='no-results']"))
    }

    override fun getSavedState(): SavedState {
        browser.waitUntilAnyPresent(
            "[data-testid='product-card']",
            "[data-testid='save-button']",
            "[data-testid='wishlist-link']",
        )
        val present = browser.isPresent("[data-testid='save-button']")
        val pressed = present && browser.attribute("[data-testid='save-button']", "aria-pressed").toBoolean()
        val enabled = present && browser.isEnabled("[data-testid='save-button']")
        return SavedState(
            saveButtonPresent = present,
            saveButtonPressed = pressed,
            saveButtonEnabled = enabled,
            savedPageCount = readSavedCount(),
            wishlistLinkVisible = browser.isPresent("[data-testid='wishlist-link']"),
        )
    }

    override fun currentUrl(): String = browser.currentUrl()

    private fun extractProductCards(): List<ProductCard> {
        @Suppress("UNCHECKED_CAST")
        return browser.extractAllViaScript("""
            return Array.from(document.querySelectorAll('[data-testid="product-card"]'))
                .map(card => ({
                    title: (card.querySelector('[data-testid="product-title"]')?.textContent ?? '').trim(),
                    price: (card.querySelector('[data-testid="product-price"]')?.textContent ?? '').trim(),
                    imageUrl: card.querySelector('img')?.src ?? ''
                }));
        """).map { m -> ProductCard(m["title"] ?: "", m["price"] ?: "", m["imageUrl"] ?: "") }
    }

    private fun readCartCount(): Int = browser.text("[data-testid='cart-count']").let { if (it.isEmpty()) 0 else it.toInt() }

    private fun readCartTotal(): String = browser.text("[data-testid='cart-total']")

    private fun readCartItems(): List<CartItem> =
        (0 until browser.count("[data-testid='cart-item']")).map { i ->
            CartItem(browser.nthText("[data-testid='cart-item']", i))
        }

    private fun readSavedCount(): Int =
        browser.text("[data-testid='saved-count']")
            .replace(Regex("[^0-9]"), "")
            .trim()
            .let { if (it.isEmpty()) 0 else it.toInt() }
}
