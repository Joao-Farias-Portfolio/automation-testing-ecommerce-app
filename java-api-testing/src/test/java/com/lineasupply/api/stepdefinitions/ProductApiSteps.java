package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductApiSteps {

    private static final String BASE_URL = "http://localhost:8001";

    private int categoryId;

    @Given("a valid category ID exists in the system")
    public void validCategoryIdExists() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/categories");
        List<Map<String, Object>> categories = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(categories).isNotEmpty();
        categoryId = (int) categories.get(0).get("id");
    }

    @When("the API consumer requests all products")
    public void requestAllProducts() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/products");
    }

    @When("the API consumer requests products for that category")
    public void requestProductsForCategory() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("categoryId", categoryId)
            .when()
            .get("/api/products");
    }

    @When("the API consumer requests products sorted by price ascending")
    public void requestProductsSortedByPriceAsc() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("sort", "price_asc")
            .when()
            .get("/api/products");
    }

    @When("the API consumer requests products sorted by price descending")
    public void requestProductsSortedByPriceDesc() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("sort", "price_desc")
            .when()
            .get("/api/products");
    }

    @When("the API consumer requests products with delivery summary")
    public void requestProductsWithDeliverySummary() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("include_delivery_summary", true)
            .when()
            .get("/api/products");
    }

    @When("the API consumer requests products with an unknown sort order")
    public void requestProductsWithUnknownSort() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("sort", "unknown_sort_value")
            .when()
            .get("/api/products");
    }

    @Then("the response should contain at least one product")
    public void responseShouldContainAtLeastOneProduct() {
        List<Object> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
    }

    @Then("each product should have an id, title, and price")
    public void eachProductShouldHaveIdTitleAndPrice() {
        List<Map<String, Object>> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
        var first = products.get(0);
        assertThat(first.keySet()).contains("id", "title", "price");
        assertThat(first.get("id")).isNotNull();
        assertThat(String.valueOf(first.get("title"))).isNotBlank();
        assertThat(first.get("price")).isNotNull();
    }

    @Then("products should include image URL fields")
    public void productsShouldIncludeImageUrlFields() {
        List<Map<String, Object>> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
        assertThat(products.get(0).keySet()).contains("image_url");
    }

    @Then("all returned products should belong to the requested category")
    public void allProductsShouldBelongToRequestedCategory() {
        List<Map<String, Object>> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
        products.forEach(p -> assertThat(p.get("category_id"))
            .as("product category_id should match requested category")
            .isEqualTo(categoryId));
    }

    @Then("products should be returned in ascending price order")
    public void productsShouldBeInAscendingPriceOrder() {
        List<Float> prices = SerenityRest.lastResponse().jsonPath().getList("price", Float.class);
        assertThat(prices).isNotEmpty();
        for (int i = 1; i < prices.size(); i++) {
            assertThat(prices.get(i)).isGreaterThanOrEqualTo(prices.get(i - 1));
        }
    }

    @Then("products should be returned in descending price order")
    public void productsShouldBeInDescendingPriceOrder() {
        List<Float> prices = SerenityRest.lastResponse().jsonPath().getList("price", Float.class);
        assertThat(prices).isNotEmpty();
        for (int i = 1; i < prices.size(); i++) {
            assertThat(prices.get(i)).isLessThanOrEqualTo(prices.get(i - 1));
        }
    }

    @Then("products should include delivery summary information")
    public void productsShouldIncludeDeliverySummary() {
        List<Map<String, Object>> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
        long withSummary = products.stream()
            .filter(p -> p.get("delivery_summary") != null)
            .count();
        assertThat(withSummary).isGreaterThan(0);
    }
}
