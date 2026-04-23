package com.myecommerce.automation.driver.api;

import com.myecommerce.automation.driver.ports.HttpPort;
import com.myecommerce.automation.dsl.domain.DeliveryOption;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductCard;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SearchResults;
import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.Channel;
import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import lombok.extern.java.Log;

import java.util.List;
import java.util.stream.IntStream;

@Log
public final class MyEcommerceDriver implements CatalogueProtocol {

    private static final String BASE_URL = "http://localhost:8001";

    static {
        DriverRegistry.register(Channel.API, () -> {
            HttpPort http = "okhttp".equals(System.getProperty("http.impl", "restassured"))
                ? new OkHttpHttpPort(BASE_URL)
                : new RestAssuredHttpPort(BASE_URL);
            return new MyEcommerceDriver(http);
        });
    }

    private final HttpPort http;
    private int currentProductId = -1;
    private String lastSearchTerm = "";

    MyEcommerceDriver(HttpPort http) {
        this.http = http;
    }

    @Override public void browseCatalogue() {}
    @Override public void returnToProductListing() {}
    @Override public void chooseAlternativeDeliveryOption() {}

    @Override
    public void viewFirstProduct() {
        this.currentProductId = fetchProducts("").getFirst().id();
        log.info("selected product id=%d".formatted(currentProductId));
    }

    @Override
    public void searchFor(String term) {
        this.lastSearchTerm = term;
    }

    @Override
    public ProductListing getProductListing() {
        var cards = fetchProducts("").stream()
            .map(p -> new ProductCard(p.title(), formatPrice(p.price()), absoluteImageUrl(p.imageUrl())))
            .toList();
        log.info("%d products".formatted(cards.size()));
        return new ProductListing(cards, false);
    }

    @Override
    public ProductDetail getProductDetail() {
        var product = fetchProductDetail(currentProductId);
        log.info("title='%s'".formatted(product.title()));
        return ProductDetail.builder()
            .title(product.title())
            .price(formatPrice(product.price()))
            .description(product.description())
            .imagePresent(product.imageUrl() != null && !product.imageUrl().isBlank())
            .addToCartButtonText("Add to Cart")
            .addToCartEnabled(true)
            .build();
    }

    @Override
    public DeliveryState getDeliveryState() {
        var activeOptions = fetchActiveDeliveryOptions();
        if (activeOptions.isEmpty()) {
            log.info("no active delivery options");
            return DeliveryState.builder()
                .sectionVisible(false)
                .options(List.of())
                .headerText("")
                .minimumOrderTextPresent(false)
                .build();
        }
        log.info("%d active delivery options".formatted(activeOptions.size()));
        return DeliveryState.builder()
            .sectionVisible(true)
            .options(toDeliveryOptions(activeOptions))
            .headerText("Delivery Options")
            .minimumOrderTextPresent(false)
            .build();
    }

    @Override
    public SearchResults getSearchResults() {
        var cards = fetchProducts(lastSearchTerm).stream()
            .map(p -> new ProductCard(p.title(), formatPrice(p.price()), absoluteImageUrl(p.imageUrl())))
            .toList();
        log.info("%d results for '%s'".formatted(cards.size(), lastSearchTerm));
        return new SearchResults(cards, cards.isEmpty());
    }

    @Override
    public String currentUrl() {
        if (!lastSearchTerm.isBlank()) return BASE_URL + "/products?search=" + lastSearchTerm;
        if (currentProductId >= 0)     return BASE_URL + "/products/" + currentProductId;
        return BASE_URL + "/products";
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<ApiProduct> fetchProducts(String searchTerm) {
        if (searchTerm.isBlank()) {
            return http.getListAs("/products", ApiProduct.class);
        }
        return http.getListWithQueryAs("/products", "search", searchTerm, ApiProduct.class);
    }

    private ApiProductDetail fetchProductDetail(int id) {
        return http.getAs("/products/" + id, ApiProductDetail.class);
    }

    private List<ApiDeliveryOption> fetchActiveDeliveryOptions() {
        return fetchProductDetail(currentProductId).deliveryOptions().stream()
            .filter(ApiDeliveryOption::isActive)
            .toList();
    }

    private List<DeliveryOption> toDeliveryOptions(List<ApiDeliveryOption> activeOptions) {
        return IntStream.range(0, activeOptions.size())
            .mapToObj(i -> new DeliveryOption(activeOptions.get(i).name(), i == 0))
            .toList();
    }

    private String formatPrice(double price) {
        return "$%.2f".formatted(price);
    }

    private String absoluteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return "";
        if (imageUrl.startsWith("http")) return imageUrl;
        return BASE_URL + imageUrl;
    }
}
