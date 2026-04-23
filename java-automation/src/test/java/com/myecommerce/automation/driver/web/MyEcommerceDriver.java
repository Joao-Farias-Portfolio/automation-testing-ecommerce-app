package com.myecommerce.automation.driver.web;

import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol;
import com.myecommerce.automation.dsl.domain.CartItem;
import com.myecommerce.automation.dsl.domain.CartState;
import com.myecommerce.automation.dsl.domain.DeliveryOption;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductCard;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SavedState;
import com.myecommerce.automation.dsl.domain.SearchResults;
import lombok.extern.java.Log;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.actors.OnStage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Log
public final class MyEcommerceDriver implements MyEcommerceProtocol {

    static {
        DriverRegistry.register("Web", MyEcommerceDriver::new);
    }

    private static final String BASE_URL = "http://localhost:3001";

    // ── Driver access ────────────────────────────────────────────────────────

    private WebDriver driver() {
        return BrowseTheWeb.as(OnStage.theActorCalled("Shopper")).getDriver();
    }

    private void navigateTo(String url) {
        OnStage.theActorCalled("Shopper").attemptsTo(Open.url(url));
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Step("Navigate to home page")
    @Override
    public void openHomePage() {
        log.info("openHomePage: navigating to " + BASE_URL);
        navigateTo(BASE_URL);
        log.fine("openHomePage: navigation complete");
    }

    @Step("Navigate to cart page")
    @Override
    public void openCartPage() {
        log.info("openCartPage: navigating to cart");
        navigateTo(BASE_URL + "/cart");
        log.fine("openCartPage: navigation complete");
    }

    @Step("Navigate to saved page")
    @Override
    public void openSavedPage() {
        log.info("openSavedPage: navigating to saved page");
        navigateTo(BASE_URL + "/saved");
        log.fine("openSavedPage: navigation complete");
    }

    @Step("Navigate back")
    @Override
    public void navigateBack() {
        log.info("navigateBack: navigating browser back");
        driver().navigate().back();
        log.fine("navigateBack: navigation complete");
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @Step("Add product to cart")
    @Override
    public void addProductToCart() {
        log.info("addProductToCart: finding first enabled add-to-cart button");
        waitUntilMoreThan(0, "[data-testid='add-to-cart']");
        var buttons = driver().findElements(By.cssSelector("[data-testid='add-to-cart']"));
        var firstEnabled = buttons.stream()
            .filter(WebElement::isEnabled)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No enabled add-to-cart button found"));
        firstEnabled.click();
        log.fine("addProductToCart: clicked first enabled add-to-cart button");
    }

    @Step("Remove first item from cart")
    @Override
    public void removeFirstItemFromCart() {
        log.info("removeFirstItemFromCart: removing first cart item");
        waitUntilMoreThan(0, "[data-testid='remove-item']");
        driver().findElements(By.cssSelector("[data-testid='remove-item']")).getFirst().click();
        log.fine("removeFirstItemFromCart: first cart item removed");
    }

    @Step("Change quantity to {0}")
    @Override
    public void changeQuantityTo(int quantity) {
        log.info("changeQuantityTo: setting quantity to " + quantity);
        waitUntilPresent("[data-testid='quantity-display']");
        var input = driver().findElement(By.cssSelector("[data-testid='quantity-display']"));
        setReactInputValue(input, String.valueOf(quantity));
        log.fine("changeQuantityTo: quantity set to " + quantity);
    }

    @Step("Search for '{0}'")
    @Override
    public void searchFor(String term) {
        log.info("searchFor: searching for '" + term + "'");
        var searchInput = driver().findElement(By.cssSelector("input[placeholder*='Search Items']"));
        searchInput.clear();
        searchInput.sendKeys(term);
        searchInput.sendKeys(Keys.ENTER);
        log.fine("searchFor: search submitted for '" + term + "'");
    }

    @Step("Click first product card")
    @Override
    public void clickFirstProductCard() {
        log.info("clickFirstProductCard: clicking first product card");
        waitUntilVisible("[data-testid='product-card']");
        driver().findElements(By.cssSelector("[data-testid='product-card']")).getFirst().click();
        waitUntilUrlMatches(".*/products/\\d+");
        log.fine("clickFirstProductCard: navigated to product detail page");
    }

    @Step("Select alternative delivery option")
    @Override
    public void selectAlternativeDeliveryOption() {
        log.info("selectAlternativeDeliveryOption: selecting a non-currently-selected delivery option");
        var section = deliverySection();
        if (section.isEmpty()) {
            log.warning("selectAlternativeDeliveryOption: no delivery section found");
            return;
        }
        var radios = section.getFirst().findElements(By.cssSelector("input[type='radio']"));
        var unselected = radios.stream()
            .filter(radio -> !radio.isSelected())
            .findFirst();
        unselected.ifPresentOrElse(
            radio -> {
                radio.findElement(By.xpath("../..")).click();
                log.fine("selectAlternativeDeliveryOption: clicked unselected delivery option");
            },
            () -> log.warning("selectAlternativeDeliveryOption: all options are already selected"));
    }

    @Step("Toggle first save button")
    @Override
    public void toggleFirstSaveButton() {
        log.info("toggleFirstSaveButton: toggling first save button");
        var button = driver().findElements(By.cssSelector("[data-testid='save-button']")).getFirst();
        var previousState = button.getAttribute("aria-pressed");
        button.click();
        waitForAriaPressed(button, previousState);
        log.fine("toggleFirstSaveButton: save button toggled from aria-pressed=" + previousState);
    }

    @Step("Click wishlist link")
    @Override
    public void clickWishlistLink() {
        log.info("clickWishlistLink: clicking wishlist link");
        driver().findElement(By.cssSelector("[data-testid='wishlist-link']")).click();
        waitUntilUrlContains("/saved");
        log.fine("clickWishlistLink: navigated to saved page");
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    public ProductListing getProductListing() {
        log.info("getProductListing: waiting for product cards");
        waitUntilVisible("[data-testid='product-card']");
        var cards = extractProductCardsViaJavascript();
        boolean loadingVisible = hasVisibleLoadingElements();
        log.info("getProductListing: found " + cards.size() + " cards, loadingVisible=" + loadingVisible);
        return new ProductListing(cards, loadingVisible);
    }

    @Override
    public CartState getCartState() {
        log.info("getCartState: reading cart state");
        var count = readCartCount();
        var total = readCartTotal();
        var items = readCartItems();
        boolean empty = isCartEmptyStateVisible();
        log.info("getCartState: count=" + count + ", total=" + total + ", items=" + items.size() + ", empty=" + empty);
        return new CartState(count, total, items, empty);
    }

    @Override
    public ProductDetail getProductDetail() {
        log.info("getProductDetail: reading product detail page");
        waitUntilVisible("[data-testid='product-title']");
        var title = textOf("[data-testid='product-title']");
        var price = textOf("[data-testid='product-price']");
        var description = textOf("[data-testid='product-description']");
        boolean imagePresent = isProductImagePresent();
        var addToCartButton = findAddToCartButton();
        log.info("getProductDetail: title='%s', imagePresent=%b, buttonEnabled=%b"
            .formatted(title, imagePresent, addToCartButton.isEnabled()));
        return new ProductDetail(title, price, description, imagePresent,
            addToCartButton.getText(), addToCartButton.isEnabled());
    }

    private boolean isProductImagePresent() {
        return !driver().findElements(By.cssSelector("[data-testid='product-detail-image']")).isEmpty();
    }

    private WebElement findAddToCartButton() {
        return driver().findElement(By.cssSelector("[data-testid='add-to-cart']"));
    }

    @Override
    public DeliveryState getDeliveryState() {
        log.info("getDeliveryState: reading delivery section");
        var sections = deliverySection();
        boolean visible = !sections.isEmpty() && sections.getFirst().isDisplayed();
        if (!visible) {
            log.info("getDeliveryState: delivery section not visible");
            return new DeliveryState(false, List.of(), "", false);
        }
        var options = readDeliveryOptions(sections.getFirst());
        var header = readDeliveryHeader();
        boolean minimumOrderPresent = minimumOrderTextPresent();
        log.info("getDeliveryState: visible=true, options=" + options.size() + ", header='" + header + "'");
        return new DeliveryState(true, options, header, minimumOrderPresent);
    }

    @Override
    public SearchResults getSearchResults() {
        log.info("getSearchResults: reading search results");
        waitUntilUrlContains("/search/");
        var cards = extractProductCardsViaJavascript();
        boolean emptyStateVisible = !driver().findElements(By.cssSelector("[data-testid='no-results']")).isEmpty();
        log.info("getSearchResults: found " + cards.size() + " cards, emptyStateVisible=" + emptyStateVisible);
        return new SearchResults(cards, emptyStateVisible);
    }

    @Override
    public SavedState getSavedState() {
        log.info("getSavedState: reading saved state");
        var saveButtons = driver().findElements(By.cssSelector("[data-testid='save-button']"));
        boolean present = !saveButtons.isEmpty();
        boolean pressed = present && isAriaPressed(saveButtons.getFirst());
        boolean enabled = present && saveButtons.getFirst().isEnabled();
        int savedCount = readSavedCount();
        boolean wishlistLinkVisible = !driver().findElements(By.cssSelector("[data-testid='wishlist-link']")).isEmpty();
        log.info("getSavedState: present=%b, pressed=%b, enabled=%b, count=%d"
            .formatted(present, pressed, enabled, savedCount));
        return new SavedState(present, pressed, enabled, savedCount, wishlistLinkVisible);
    }

    private boolean isAriaPressed(WebElement el) {
        return "true".equals(el.getAttribute("aria-pressed"));
    }

    @Override
    public String currentUrl() {
        var url = driver().getCurrentUrl();
        log.fine("currentUrl: " + url);
        return url;
    }

    // ── Synchronization ──────────────────────────────────────────────────────

    @Override
    public void waitForCartCountToBe(int expected) {
        log.info("waitForCartCountToBe: waiting for cart count to be " + expected);
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.textToBe(
                By.cssSelector("[data-testid='cart-count']"), String.valueOf(expected)));
        log.fine("waitForCartCountToBe: cart count is now " + expected);
    }

    @Override
    public void waitForCartTotalToChange(String previousTotal) {
        log.info("waitForCartTotalToChange: waiting for total to change from '" + previousTotal + "'");
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(_ -> {
                var totals = driver().findElements(By.cssSelector("[data-testid='cart-total']"));
                return !totals.isEmpty() && !totals.getFirst().getText().trim().equals(previousTotal);
            });
        log.fine("waitForCartTotalToChange: total has changed from '" + previousTotal + "'");
    }

    @Override
    public void waitForCartToBeEmpty() {
        log.info("waitForCartToBeEmpty: waiting for empty cart state");
        new WebDriverWait(driver(), Duration.ofSeconds(15))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='empty-cart']")));
        log.fine("waitForCartToBeEmpty: empty cart state is visible");
    }

    @Override
    public void waitForCartItemsToAppear() {
        log.info("waitForCartItemsToAppear: waiting for cart items to appear");
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='cart-item']"), 0));
        log.fine("waitForCartItemsToAppear: cart items are visible");
    }

    @Override
    public void waitForProductsToLoad() {
        log.info("waitForProductsToLoad: waiting for product cards");
        waitUntilVisible("[data-testid='product-card']");
        log.fine("waitForProductsToLoad: product cards are visible");
    }

    @Override
    public void waitForSearchResultsToLoad() {
        log.info("waitForSearchResultsToLoad: waiting for search URL");
        waitUntilUrlContains("/search/");
        log.fine("waitForSearchResultsToLoad: on search results page");
    }

    @Override
    public void waitForSavedPageToLoad() {
        log.info("waitForSavedPageToLoad: waiting for saved page URL");
        waitUntilUrlContains("/saved");
        log.fine("waitForSavedPageToLoad: on saved page");
    }

    // ── Browse helpers ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<ProductCard> extractProductCardsViaJavascript() {
        var rawData = (List<Map<String, String>>) ((org.openqa.selenium.JavascriptExecutor) driver())
            .executeScript("""
                return Array.from(document.querySelectorAll('[data-testid="product-card"]'))
                    .map(card => ({
                        title: (card.querySelector('[data-testid="product-title"]')?.textContent ?? '').trim(),
                        price: (card.querySelector('[data-testid="product-price"]')?.textContent ?? '').trim(),
                        imageUrl: card.querySelector('img')?.src ?? ''
                    }));
                """);
        return rawData.stream()
            .map(m -> new ProductCard(m.get("title"), m.get("price"), m.get("imageUrl")))
            .toList();
    }

    private boolean hasVisibleLoadingElements() {
        return driver().findElements(By.cssSelector("[data-testid='loading']"))
            .stream()
            .anyMatch(this::safeIsDisplayed);
    }

    // ── Cart helpers ─────────────────────────────────────────────────────────

    private int readCartCount() {
        var badges = driver().findElements(By.cssSelector("[data-testid='cart-count']"));
        if (badges.isEmpty()) return 0;
        String text = badges.getFirst().getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private String readCartTotal() {
        var totals = driver().findElements(By.cssSelector("[data-testid='cart-total']"));
        return totals.isEmpty() ? "" : totals.getFirst().getText().trim();
    }

    private List<CartItem> readCartItems() {
        return driver().findElements(By.cssSelector("[data-testid='cart-item']"))
            .stream()
            .map(el -> new CartItem(el.getText().trim()))
            .toList();
    }

    private boolean isCartEmptyStateVisible() {
        return !driver().findElements(By.cssSelector("[data-testid='empty-cart']")).isEmpty();
    }

    // ── Delivery helpers ─────────────────────────────────────────────────────

    private static final String DELIVERY_SELECTOR =
        "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
        "[data-testid='shipping-section'], [data-testid='shipping-options']";

    private List<WebElement> deliverySection() {
        return driver().findElements(By.cssSelector(DELIVERY_SELECTOR));
    }

    private List<DeliveryOption> readDeliveryOptions(WebElement section) {
        return section.findElements(By.cssSelector("input[type='radio']"))
            .stream()
            .map(radio -> new DeliveryOption(radio.getText(), radio.isSelected()))
            .toList();
    }

    private String readDeliveryHeader() {
        var headers = driver().findElements(
            By.xpath("//*[contains(translate(text(), 'DELIVERYOPTIONS', 'deliveryoptions'), 'delivery options')]"));
        return headers.isEmpty() ? "" : headers.getFirst().getText().trim();
    }

    private boolean minimumOrderTextPresent() {
        return !driver().findElements(
            By.xpath("//*[contains(text(), 'Minimum order') or contains(text(), 'Min order')]")).isEmpty();
    }

    // ── Saved helpers ────────────────────────────────────────────────────────

    private int readSavedCount() {
        var counts = driver().findElements(By.cssSelector("[data-testid='saved-count']"));
        if (counts.isEmpty()) return 0;
        String number = counts.getFirst().getText().replaceAll("[^0-9]", "").trim();
        return number.isEmpty() ? 0 : Integer.parseInt(number);
    }

    private void waitForAriaPressed(WebElement button, String previousValue) {
        new WebDriverWait(driver(), Duration.ofSeconds(5))
            .until(_ -> !previousValue.equals(button.getAttribute("aria-pressed")));
    }

    // ── DOM utilities ────────────────────────────────────────────────────────

    private void waitUntilVisible(String css) {
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    private void waitUntilPresent(String css) {
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
    }

    private void waitUntilMoreThan(int count, String css) {
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(css), count));
    }

    private void waitUntilUrlContains(String fragment) {
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains(fragment));
    }

    private void waitUntilUrlMatches(String regex) {
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.urlMatches(regex));
    }

    private void clickAtIndex(String css, int index) {
        driver().findElements(By.cssSelector(css)).get(index).click();
    }

    private String textOf(String css) {
        var elements = driver().findElements(By.cssSelector(css));
        return elements.isEmpty() ? "" : elements.getFirst().getText().trim();
    }

    private String textOf(WebElement parent, String css) {
        return parent.findElements(By.cssSelector(css))
            .stream()
            .findFirst()
            .map(WebElement::getText)
            .map(String::trim)
            .orElse("");
    }

    private String attrOf(WebElement parent, String css, String attribute) {
        return parent.findElements(By.cssSelector(css))
            .stream()
            .findFirst()
            .map(el -> el.getAttribute(attribute))
            .orElse("");
    }

    private void setReactInputValue(WebElement input, String value) {
        var js = (org.openqa.selenium.JavascriptExecutor) driver();
        js.executeScript("""
            var setter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype, 'value').set;
            setter.call(arguments[0], arguments[1]);
            arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
            arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
            """, input, value);
    }

    private boolean safeIsDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception _) {
            return false;
        }
    }
}
