package com.myecommerce.automation.driver.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.myecommerce.automation.dsl.domain.DeliveryOption;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductCard;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SearchResults;
import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.Channel;
import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.ObjectMapperConfig;
import lombok.extern.java.Log;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.stream.IntStream;

@Log
public final class MyEcommerceDriver implements CatalogueProtocol {

    static {
        DriverRegistry.register(Channel.API, MyEcommerceDriver::new);
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
            .map(product -> new ProductCard(product.title(), formatPrice(product.price()), absoluteImageUrl(product.imageUrl())))
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
        var activeDeliveryOptions = fetchActiveDeliveryOptions();
        if (activeDeliveryOptions.isEmpty()) {
            log.info("no active delivery options");
            return DeliveryState.builder()
                .sectionVisible(false)
                .options(List.of())
                .headerText("")
                .minimumOrderTextPresent(false)
                .build();
        }
        log.info("%d active delivery options".formatted(activeDeliveryOptions.size()));
        return DeliveryState.builder()
            .sectionVisible(true)
            .options(toDeliveryOptions(activeDeliveryOptions))
            .headerText("Delivery Options")
            .minimumOrderTextPresent(false)
            .build();
    }

    @Override
    public SearchResults getSearchResults() {
        var cards = fetchProducts(lastSearchTerm).stream()
            .map(product -> new ProductCard(product.title(), formatPrice(product.price()), absoluteImageUrl(product.imageUrl())))
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

    private List<ApiDeliveryOption> fetchActiveDeliveryOptions() {
        return fetchProductDetail(currentProductId).deliveryOptions().stream()
            .filter(ApiDeliveryOption::isActive)
            .toList();
    }

    private List<DeliveryOption> toDeliveryOptions(List<ApiDeliveryOption> activeOptions) {
        return IntStream.range(0, activeOptions.size())
            .mapToObj(index -> new DeliveryOption(activeOptions.get(index).name(), index == 0))
            .toList();
    }

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
}
