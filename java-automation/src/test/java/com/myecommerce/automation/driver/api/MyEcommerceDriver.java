package com.myecommerce.automation.driver.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.myecommerce.automation.dsl.domain.CartState;
import com.myecommerce.automation.dsl.domain.DeliveryOption;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductCard;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SavedState;
import com.myecommerce.automation.dsl.domain.SearchResults;
import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.ObjectMapperConfig;
import lombok.extern.java.Log;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.stream.IntStream;

@Log
public final class MyEcommerceDriver implements MyEcommerceProtocol {

    static {
        DriverRegistry.register("API", MyEcommerceDriver::new);
        RestAssured.config = RestAssured.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig()
                .jackson2ObjectMapperFactory((cls, charset) ->
                    new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)));
    }

    private static final String BASE_URL = "http://localhost:8001";

    private int currentProductId = -1;
    private String lastSearchTerm = "";

    // ── Navigation (API is stateless — no browser, no pages) ────────────────────

    @Override
    public void openHomePage() {
        log.info("openHomePage: no-op for API channel");
    }

    @Override
    public void openCartPage() {
        throw unsupported("Cart page");
    }

    @Override
    public void openSavedPage() {
        throw unsupported("Saved page");
    }

    @Override
    public void navigateBack() {
        log.info("navigateBack: no-op for API channel");
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    @Override
    public void clickFirstProductCard() {
        log.info("clickFirstProductCard: resolving first product via GET /products");
        this.currentProductId = fetchProducts("").getFirst().id();
        log.info("clickFirstProductCard: selected product id=" + currentProductId);
    }

    @Override
    public void searchFor(String term) {
        log.info("searchFor: storing search term '" + term + "'");
        this.lastSearchTerm = term;
    }

    @Override
    public void addProductToCart() {
        throw unsupported("Cart");
    }

    @Override
    public void removeFirstItemFromCart() {
        throw unsupported("Cart");
    }

    @Override
    public void changeQuantityTo(int quantity) {
        throw unsupported("Cart");
    }

    @Override
    public void selectAlternativeDeliveryOption() {
        throw unsupported("Delivery option selection");
    }

    @Override
    public void toggleFirstSaveButton() {
        throw unsupported("Save button");
    }

    @Override
    public void clickWishlistLink() {
        throw unsupported("Wishlist link");
    }

    // ── Queries ──────────────────────────────────────────────────────────────────

    @Override
    public ProductListing getProductListing() {
        log.info("getProductListing: GET /products");
        var cards = fetchProducts("").stream()
            .map(p -> new ProductCard(p.title(), formatPrice(p.price()), absoluteImageUrl(p.imageUrl())))
            .toList();
        log.info("getProductListing: " + cards.size() + " products");
        return new ProductListing(cards, false);
    }

    @Override
    public ProductDetail getProductDetail() {
        log.info("getProductDetail: GET /products/" + currentProductId);
        var p = fetchProductDetail(currentProductId);
        log.info("getProductDetail: title='" + p.title() + "'");
        return new ProductDetail(
            p.title(),
            formatPrice(p.price()),
            p.description(),
            p.imageUrl() != null && !p.imageUrl().isBlank(),
            "Add to Cart",
            true);
    }

    @Override
    public DeliveryState getDeliveryState() {
        log.info("getDeliveryState: GET /products/" + currentProductId);
        var p = fetchProductDetail(currentProductId);
        var activeOptions = p.deliveryOptions().stream()
            .filter(ApiDeliveryOption::isActive)
            .toList();
        if (activeOptions.isEmpty()) {
            log.info("getDeliveryState: no active delivery options");
            return new DeliveryState(false, List.of(), "", false);
        }
        var options = IntStream.range(0, activeOptions.size())
            .mapToObj(i -> new DeliveryOption(activeOptions.get(i).name(), i == 0))
            .toList();
        // Frontend never renders min_order_amount — always false regardless of backend data
        log.info("getDeliveryState: " + options.size() + " options");
        return new DeliveryState(true, options, "Delivery Options", false);
    }

    @Override
    public SearchResults getSearchResults() {
        log.info("getSearchResults: GET /products?search=" + lastSearchTerm);
        var cards = fetchProducts(lastSearchTerm).stream()
            .map(p -> new ProductCard(p.title(), formatPrice(p.price()), absoluteImageUrl(p.imageUrl())))
            .toList();
        log.info("getSearchResults: " + cards.size() + " results for '" + lastSearchTerm + "'");
        return new SearchResults(cards, cards.isEmpty());
    }

    @Override
    public CartState getCartState() {
        throw unsupported("Cart state");
    }

    @Override
    public SavedState getSavedState() {
        throw unsupported("Save state");
    }

    @Override
    public String currentUrl() {
        if (!lastSearchTerm.isBlank()) return BASE_URL + "/products?search=" + lastSearchTerm;
        if (currentProductId >= 0)     return BASE_URL + "/products/" + currentProductId;
        return BASE_URL + "/products";
    }

    // ── Synchronisation (all no-ops — REST Assured responses are synchronous) ────

    @Override public void waitForCartCountToBe(int expected)        { /* no-op */ }
    @Override public void waitForCartTotalToChange(String prev)     { /* no-op */ }
    @Override public void waitForCartToBeEmpty()                    { /* no-op */ }
    @Override public void waitForCartItemsToAppear()                { /* no-op */ }
    @Override public void waitForProductsToLoad()                   { /* no-op */ }
    @Override public void waitForSearchResultsToLoad()              { /* no-op */ }
    @Override public void waitForSavedPageToLoad()                  { /* no-op */ }

    // ── HTTP helpers ─────────────────────────────────────────────────────────────

    private List<ApiProduct> fetchProducts(String searchTerm) {
        var spec = SerenityRest.given().baseUri(BASE_URL);
        if (!searchTerm.isBlank()) {
            spec = spec.queryParam("search", searchTerm);
        }
        return spec.get("/products")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {});
    }

    private ApiProductDetail fetchProductDetail(int id) {
        return SerenityRest.given().baseUri(BASE_URL)
            .get("/products/" + id)
            .then().statusCode(200)
            .extract().as(ApiProductDetail.class);
    }

    private String formatPrice(double price) {
        return "$%.2f".formatted(price);
    }

    private String absoluteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return "";
        if (imageUrl.startsWith("http")) return imageUrl;
        return BASE_URL + imageUrl;
    }

    private UnsupportedOperationException unsupported(String feature) {
        return new UnsupportedOperationException(
            feature + " operations require the Web channel — run with -Dchannel=Web");
    }
}
