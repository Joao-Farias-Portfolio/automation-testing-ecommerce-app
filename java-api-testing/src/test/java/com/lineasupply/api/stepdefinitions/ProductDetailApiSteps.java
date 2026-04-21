package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductDetailApiSteps {

    private static final String BASE_URL = "http://localhost:8001";

    private int productId;

    @Given("a valid product ID exists in the system")
    public void validProductIdExists() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/products");
        List<Map<String, Object>> products = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
        productId = (int) products.get(0).get("id");
    }

    @When("the API consumer requests that product by ID")
    public void requestProductById() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .when()
            .get("/products/" + productId);
    }

    @When("the API consumer requests a product with ID {int}")
    public void requestProductWithId(int id) {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .when()
            .get("/products/" + id);
    }

    @When("the API consumer requests the image for that product")
    public void requestProductImage() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .when()
            .get("/products/" + productId + "/image");
    }

    @Then("the product should have a title, description, and price")
    public void productShouldHaveTitleDescriptionAndPrice() {
        var body = SerenityRest.lastResponse().jsonPath();
        assertThat(body.getString("title")).isNotBlank();
        assertThat(body.getString("description")).isNotBlank();
        assertThat(body.getString("price")).isNotNull();
    }

    @Then("the product should include delivery options")
    public void productShouldIncludeDeliveryOptions() {
        List<Object> deliveryOptions = SerenityRest.lastResponse()
            .jsonPath().getList("delivery_options");
        assertThat(deliveryOptions).isNotEmpty();
    }

    @Then("the product should include category information")
    public void productShouldIncludeCategoryInformation() {
        var category = SerenityRest.lastResponse().jsonPath().getMap("category");
        assertThat(category).isNotNull();
        assertThat(category.get("id")).isNotNull();
        assertThat(String.valueOf(category.get("name"))).isNotBlank();
    }

    @Then("the response content type should be an image type")
    public void responseContentTypeShouldBeImageType() {
        String contentType = SerenityRest.lastResponse().contentType();
        assertThat(contentType).matches("image/.*");
    }
}
