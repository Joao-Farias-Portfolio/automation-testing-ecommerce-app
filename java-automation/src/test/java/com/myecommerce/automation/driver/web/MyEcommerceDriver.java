package com.myecommerce.automation.driver.web;

import com.myecommerce.automation.driver.ports.BrowserPort;
import com.myecommerce.automation.dsl.domain.CartItem;
import com.myecommerce.automation.dsl.domain.CartState;
import com.myecommerce.automation.dsl.domain.DeliveryOption;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductCard;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SavedState;
import com.myecommerce.automation.dsl.domain.SearchResults;
import com.myecommerce.automation.dsl.protocols.Channel;
import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol;
import lombok.extern.java.Log;
import net.serenitybdd.annotations.Step;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Log
public final class MyEcommerceDriver implements MyEcommerceProtocol {

    static {
        DriverRegistry.register(Channel.WEB, () -> {
            BrowserPort browser = "playwright".equals(System.getProperty("browser.impl", "selenium"))
                ? PlaywrightBrowserPort.fromCurrentPage()
                : new SeleniumBrowserPort();
            return new MyEcommerceDriver(browser);
        });
    }

    private static final String BASE_URL = "http://localhost:3001";

    private final BrowserPort browser;

    MyEcommerceDriver(BrowserPort browser) {
        this.browser = browser;
    }

    @Step("Browse product catalogue")
    @Override
    public void browseCatalogue() {
        log.info("browseCatalogue: navigating to " + BASE_URL);
        browser.navigateTo(BASE_URL);
    }

    @Step("View cart")
    @Override
    public void viewCart() {
        log.info("viewCart: navigating to cart");
        browser.navigateTo(BASE_URL + "/cart");
        browser.waitUntilAnyPresent("[data-testid='cart-item']", "[data-testid='empty-cart']");
    }

    @Step("View saved items")
    @Override
    public void viewSavedItems() {
        log.info("viewSavedItems: navigating to saved page");
        browser.navigateTo(BASE_URL + "/saved");
        browser.waitUntilUrlContains("/saved");
    }

    @Step("Return to product listing")
    @Override
    public void returnToProductListing() {
        log.info("returnToProductListing: navigating browser back");
        browser.navigateBack();
    }

    @Step("Add product to cart")
    @Override
    public void addProductToCart() {
        log.info("addProductToCart: finding first enabled add-to-cart button");
        browser.waitUntilCountMoreThan("[data-testid='add-to-cart']", 0);
        var countBefore = readCartCount();
        int buttonCount = browser.count("[data-testid='add-to-cart']");
        int enabledIndex = IntStream.range(0, buttonCount)
            .filter(i -> browser.isNthEnabled("[data-testid='add-to-cart']", i))
            .findFirst()
            .orElse(0);
        browser.clickNth("[data-testid='add-to-cart']", enabledIndex);
        browser.waitUntilCondition(() -> readCartCount() > countBefore, 10);
        log.fine("addProductToCart: cart count increased from " + countBefore);
    }

    @Step("Remove first item from cart")
    @Override
    public void removeFirstItemFromCart() {
        log.info("removeFirstItemFromCart: removing first cart item");
        browser.waitUntilCountMoreThan("[data-testid='remove-item']", 0);
        var countBefore = readCartCount();
        browser.clickNth("[data-testid='remove-item']", 0);
        browser.waitUntilCondition(
            () -> readCartCount() < countBefore || browser.isPresent("[data-testid='empty-cart']"), 10);
        log.fine("removeFirstItemFromCart: item removed");
    }

    @Step("Change quantity to {0}")
    @Override
    public void changeQuantityTo(int quantity) {
        log.info("changeQuantityTo: setting quantity to " + quantity);
        browser.waitUntilPresent("[data-testid='quantity-display']");
        var totalBefore = readCartTotal();
        browser.setReactInputValue("[data-testid='quantity-display']", String.valueOf(quantity));
        browser.waitUntilCondition(() -> !readCartTotal().equals(totalBefore), 10);
        log.fine("changeQuantityTo: total updated from '" + totalBefore + "'");
    }

    @Step("Search for '{0}'")
    @Override
    public void searchFor(String term) {
        log.info("searchFor: searching for '" + term + "'");
        browser.sendKeys("input[placeholder*='Search Items']", term, true);
        browser.waitUntilUrlContains("/search/");
    }

    @Step("View first product")
    @Override
    public void viewFirstProduct() {
        log.info("viewFirstProduct: clicking first product card");
        browser.waitUntilVisible("[data-testid='product-card']");
        browser.clickNth("[data-testid='product-card']", 0);
        browser.waitUntilUrlMatches(".*/products/\\d+");
    }

    @Step("Choose alternative delivery option")
    @Override
    public void chooseAlternativeDeliveryOption() {
        log.info("chooseAlternativeDeliveryOption: selecting a non-currently-selected delivery option");
        int radioCount = browser.count("input[type='radio']");
        for (int i = 0; i < radioCount; i++) {
            if (!browser.isNthSelected("input[type='radio']", i)) {
                browser.clickXpath("(//input[@type='radio'])[" + (i + 1) + "]/../../..");
                log.fine("chooseAlternativeDeliveryOption: clicked option at index " + i);
                return;
            }
        }
        log.warning("chooseAlternativeDeliveryOption: all options already selected");
    }

    @Step("Ensure first product is saved")
    @Override
    public void ensureFirstProductIsSaved() {
        browser.waitUntilVisible("[data-testid='save-button']");
        boolean pressed = Boolean.parseBoolean(browser.attribute("[data-testid='save-button']", "aria-pressed"));
        if (!pressed) {
            toggleSaveStateOfFirstProduct();
        }
    }

    @Step("Toggle save state of first product")
    @Override
    public void toggleSaveStateOfFirstProduct() {
        log.info("toggleSaveStateOfFirstProduct: toggling first save button");
        var previousState = browser.attribute("[data-testid='save-button']", "aria-pressed");
        browser.clickNth("[data-testid='save-button']", 0);
        browser.waitUntilAttributeChanges("[data-testid='save-button']", 0, "aria-pressed", previousState);
    }

    @Step("View wishlist")
    @Override
    public void viewWishlist() {
        log.info("viewWishlist: clicking wishlist link");
        browser.click("[data-testid='wishlist-link']");
        browser.waitUntilUrlContains("/saved");
    }

    @Override
    public ProductListing getProductListing() {
        log.info("getProductListing: waiting for product cards");
        browser.waitUntilVisible("[data-testid='product-card']");
        var cards = extractProductCards();
        boolean loadingVisible = browser.isPresent("[data-testid='loading']") && browser.isVisible("[data-testid='loading']");
        log.info("getProductListing: found " + cards.size() + " cards, loadingVisible=" + loadingVisible);
        return new ProductListing(cards, loadingVisible);
    }

    @Override
    public CartState getCartState() {
        log.info("getCartState: reading cart state");
        var count = readCartCount();
        var total = readCartTotal();
        var items = readCartItems();
        boolean empty = browser.isPresent("[data-testid='empty-cart']");
        log.info("getCartState: count=" + count + ", total=" + total + ", items=" + items.size() + ", empty=" + empty);
        return new CartState(count, total, items, empty);
    }

    @Override
    public ProductDetail getProductDetail() {
        log.info("getProductDetail: reading product detail page");
        browser.waitUntilVisible("[data-testid='product-title']");
        var title = browser.text("[data-testid='product-title']");
        var price = browser.text("[data-testid='product-price']");
        var description = browser.text("[data-testid='product-description']");
        boolean imagePresent = browser.isPresent("[data-testid='product-detail-image']");
        var buttonText = browser.text("[data-testid='add-to-cart']");
        boolean buttonEnabled = browser.isEnabled("[data-testid='add-to-cart']");
        log.info("getProductDetail: title='%s', imagePresent=%b, buttonEnabled=%b"
            .formatted(title, imagePresent, buttonEnabled));
        return ProductDetail.builder()
            .title(title)
            .price(price)
            .description(description)
            .imagePresent(imagePresent)
            .addToCartButtonText(buttonText)
            .addToCartEnabled(buttonEnabled)
            .build();
    }

    @Override
    public DeliveryState getDeliveryState() {
        log.info("getDeliveryState: reading delivery section");
        var section = findVisibleDeliverySection();
        if (section.isEmpty()) {
            log.info("getDeliveryState: delivery section not visible");
            return DeliveryState.builder()
                .sectionVisible(false)
                .options(List.of())
                .headerText("")
                .minimumOrderTextPresent(false)
                .build();
        }
        return presentDeliveryState(section.get());
    }

    @Override
    public SearchResults getSearchResults() {
        log.info("getSearchResults: reading search results");
        browser.waitUntilUrlContains("/search/");
        var cards = extractProductCards();
        boolean emptyStateVisible = browser.isPresent("[data-testid='no-results']");
        log.info("getSearchResults: found " + cards.size() + " cards, emptyStateVisible=" + emptyStateVisible);
        return new SearchResults(cards, emptyStateVisible);
    }

    @Override
    public SavedState getSavedState() {
        log.info("getSavedState: reading saved state");
        browser.waitUntilAnyPresent(
            "[data-testid='product-card']",
            "[data-testid='save-button']",
            "[data-testid='wishlist-link']");
        boolean present = browser.isPresent("[data-testid='save-button']");
        boolean pressed = present && Boolean.parseBoolean(browser.attribute("[data-testid='save-button']", "aria-pressed"));
        boolean enabled = present && browser.isEnabled("[data-testid='save-button']");
        int savedCount = readSavedCount();
        boolean wishlistLinkVisible = browser.isPresent("[data-testid='wishlist-link']");
        log.info("getSavedState: present=%b, pressed=%b, enabled=%b, count=%d"
            .formatted(present, pressed, enabled, savedCount));
        return new SavedState(present, pressed, enabled, savedCount, wishlistLinkVisible);
    }

    @Override
    public String currentUrl() {
        var url = browser.currentUrl();
        log.fine("currentUrl: " + url);
        return url;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Optional<String> findVisibleDeliverySection() {
        browser.waitUntilVisible("[data-testid='product-title']");
        boolean found = browser.tryWaitUntilPresent(DELIVERY_SELECTOR, 5);
        if (!found || !browser.isVisible(DELIVERY_SELECTOR)) {
            return Optional.empty();
        }
        return Optional.of(DELIVERY_SELECTOR);
    }

    private DeliveryState presentDeliveryState(String sectionCss) {
        int radioCount = browser.count("input[type='radio']");
        var options = IntStream.range(0, radioCount)
            .mapToObj(i -> {
                String id = browser.nthAttribute("input[type='radio']", i, "value");
                boolean selected = browser.isNthSelected("input[type='radio']", i);
                return new DeliveryOption(id.isBlank() ? String.valueOf(i) : id, selected);
            })
            .toList();
        var header = browser.nthText(sectionCss + " p", 0);
        boolean minimumOrderPresent = browser.isPresent(
            "[data-testid='minimum-order'], [data-testid='min-order']");
        log.info("getDeliveryState: visible=true, options=%d, header='%s'".formatted(options.size(), header));
        return DeliveryState.builder()
            .sectionVisible(true)
            .options(options)
            .headerText(header)
            .minimumOrderTextPresent(minimumOrderPresent)
            .build();
    }

    private List<ProductCard> extractProductCards() {
        return browser.extractAllViaScript("""
                return Array.from(document.querySelectorAll('[data-testid="product-card"]'))
                    .map(card => ({
                        title: (card.querySelector('[data-testid="product-title"]')?.textContent ?? '').trim(),
                        price: (card.querySelector('[data-testid="product-price"]')?.textContent ?? '').trim(),
                        imageUrl: card.querySelector('img')?.src ?? ''
                    }));
                """)
            .stream()
            .map(m -> new ProductCard(m.get("title"), m.get("price"), m.get("imageUrl")))
            .toList();
    }

    private int readCartCount() {
        String text = browser.text("[data-testid='cart-count']");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private String readCartTotal() {
        return browser.text("[data-testid='cart-total']");
    }

    private List<CartItem> readCartItems() {
        int count = browser.count("[data-testid='cart-item']");
        return IntStream.range(0, count)
            .mapToObj(i -> new CartItem(browser.nthText("[data-testid='cart-item']", i)))
            .toList();
    }

    private int readSavedCount() {
        String number = browser.text("[data-testid='saved-count']").replaceAll("[^0-9]", "").trim();
        return number.isEmpty() ? 0 : Integer.parseInt(number);
    }

    private static final String DELIVERY_SELECTOR =
        "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
        "[data-testid='shipping-section'], [data-testid='shipping-options']";
}
