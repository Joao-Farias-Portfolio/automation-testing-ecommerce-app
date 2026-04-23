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

    private WebDriver driver() {
        return BrowseTheWeb.as(OnStage.theActorCalled("Shopper")).getDriver();
    }

    private void navigateTo(String url) {
        OnStage.theActorCalled("Shopper").attemptsTo(Open.url(url));
    }

    @Step("Browse product catalogue")
    @Override
    public void browseCatalogue() {
        log.info("browseCatalogue: navigating to " + BASE_URL);
        navigateTo(BASE_URL);
    }

    @Step("View cart")
    @Override
    public void viewCart() {
        log.info("viewCart: navigating to cart");
        navigateTo(BASE_URL + "/cart");
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("[data-testid='cart-item']"), 0),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='empty-cart']"))));
    }

    @Step("View saved items")
    @Override
    public void viewSavedItems() {
        log.info("viewSavedItems: navigating to saved page");
        navigateTo(BASE_URL + "/saved");
        waitUntilUrlContains("/saved");
    }

    @Step("Return to product listing")
    @Override
    public void returnToProductListing() {
        log.info("returnToProductListing: navigating browser back");
        driver().navigate().back();
    }

    @Step("Add product to cart")
    @Override
    public void addProductToCart() {
        log.info("addProductToCart: finding first enabled add-to-cart button");
        waitUntilMoreThan(0, "[data-testid='add-to-cart']");
        var countBefore = readCartCount();
        var buttons = driver().findElements(By.cssSelector("[data-testid='add-to-cart']"));
        var firstEnabled = buttons.stream()
            .filter(WebElement::isEnabled)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No enabled add-to-cart button found"));
        firstEnabled.click();
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(_ -> readCartCount() > countBefore);
        log.fine("addProductToCart: cart count increased from " + countBefore);
    }

    @Step("Remove first item from cart")
    @Override
    public void removeFirstItemFromCart() {
        log.info("removeFirstItemFromCart: removing first cart item");
        waitUntilMoreThan(0, "[data-testid='remove-item']");
        var countBefore = readCartCount();
        driver().findElements(By.cssSelector("[data-testid='remove-item']")).getFirst().click();
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(_ -> readCartCount() < countBefore || isCartEmptyStateVisible());
        log.fine("removeFirstItemFromCart: item removed");
    }

    @Step("Change quantity to {0}")
    @Override
    public void changeQuantityTo(int quantity) {
        log.info("changeQuantityTo: setting quantity to " + quantity);
        waitUntilPresent("[data-testid='quantity-display']");
        var totalBefore = readCartTotal();
        var input = driver().findElement(By.cssSelector("[data-testid='quantity-display']"));
        setReactInputValue(input, String.valueOf(quantity));
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(_ -> !readCartTotal().equals(totalBefore));
        log.fine("changeQuantityTo: total updated from '" + totalBefore + "'");
    }

    @Step("Search for '{0}'")
    @Override
    public void searchFor(String term) {
        log.info("searchFor: searching for '" + term + "'");
        var searchInput = driver().findElement(By.cssSelector("input[placeholder*='Search Items']"));
        searchInput.clear();
        searchInput.sendKeys(term);
        searchInput.sendKeys(Keys.ENTER);
        waitUntilUrlContains("/search/");
    }

    @Step("View first product")
    @Override
    public void viewFirstProduct() {
        log.info("viewFirstProduct: clicking first product card");
        waitUntilVisible("[data-testid='product-card']");
        driver().findElements(By.cssSelector("[data-testid='product-card']")).getFirst().click();
        waitUntilUrlMatches(".*/products/\\d+");
    }

    @Step("Choose alternative delivery option")
    @Override
    public void chooseAlternativeDeliveryOption() {
        log.info("chooseAlternativeDeliveryOption: selecting a non-currently-selected delivery option");
        var section = deliverySection();
        if (section.isEmpty()) {
            log.warning("chooseAlternativeDeliveryOption: no delivery section found");
            return;
        }
        var radios = section.getFirst().findElements(By.cssSelector("input[type='radio']"));
        var unselected = radios.stream()
            .filter(radio -> !radio.isSelected())
            .findFirst();
        unselected.ifPresentOrElse(
            radio -> {
                radio.findElement(By.xpath("../..")).click();
                log.fine("chooseAlternativeDeliveryOption: clicked unselected delivery option");
            },
            () -> log.warning("chooseAlternativeDeliveryOption: all options are already selected"));
    }

    @Step("Ensure first product is saved")
    @Override
    public void ensureFirstProductIsSaved() {
        waitUntilVisible("[data-testid='save-button']");
        if (!isAriaPressed(driver().findElements(By.cssSelector("[data-testid='save-button']")).getFirst())) {
            toggleSaveStateOfFirstProduct();
        }
    }

    @Step("Toggle save state of first product")
    @Override
    public void toggleSaveStateOfFirstProduct() {
        log.info("toggleSaveStateOfFirstProduct: toggling first save button");
        var button = driver().findElements(By.cssSelector("[data-testid='save-button']")).getFirst();
        var previousState = button.getAttribute("aria-pressed");
        button.click();
        waitForAriaPressed(button, previousState);
    }

    @Step("View wishlist")
    @Override
    public void viewWishlist() {
        log.info("viewWishlist: clicking wishlist link");
        driver().findElement(By.cssSelector("[data-testid='wishlist-link']")).click();
        waitUntilUrlContains("/saved");
    }

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
        return ProductDetail.builder()
            .title(title)
            .price(price)
            .description(description)
            .imagePresent(imagePresent)
            .addToCartButtonText(addToCartButton.getText())
            .addToCartEnabled(addToCartButton.isEnabled())
            .build();
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
            return DeliveryState.builder()
                .sectionVisible(false)
                .options(List.of())
                .headerText("")
                .minimumOrderTextPresent(false)
                .build();
        }
        var options = readDeliveryOptions(sections.getFirst());
        var header = readDeliveryHeader();
        boolean minimumOrderPresent = minimumOrderTextPresent();
        log.info("getDeliveryState: visible=true, options=" + options.size() + ", header='" + header + "'");
        return DeliveryState.builder()
            .sectionVisible(true)
            .options(options)
            .headerText(header)
            .minimumOrderTextPresent(minimumOrderPresent)
            .build();
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
        new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='product-card']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='save-button']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='wishlist-link']"))));
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
        return Boolean.parseBoolean(el.getAttribute("aria-pressed"));
    }

    @Override
    public String currentUrl() {
        var url = driver().getCurrentUrl();
        log.fine("currentUrl: " + url);
        return url;
    }

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
