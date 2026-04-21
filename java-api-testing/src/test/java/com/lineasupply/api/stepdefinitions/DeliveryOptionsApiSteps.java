package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryOptionsApiSteps {

    private static final String BASE_URL = "http://localhost:8001";

    private int deliveryOptionId;

    @Given("a valid delivery option ID exists in the system")
    public void validDeliveryOptionIdExists() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/delivery-options");
        List<Map<String, Object>> options = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(options).isNotEmpty();
        deliveryOptionId = (int) options.get(0).get("id");
    }

    @When("the API consumer requests all delivery options")
    public void requestAllDeliveryOptions() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/delivery-options");
    }

    @When("the API consumer requests products for that delivery option")
    public void requestProductsForDeliveryOption() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .queryParam("deliveryOptionId", deliveryOptionId)
            .when()
            .get("/api/products");
    }

    @Then("the response should contain at least one delivery option")
    public void responseShouldContainAtLeastOneDeliveryOption() {
        List<Object> options = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(options).isNotEmpty();
    }

    @Then("each delivery option should have a name and price")
    public void eachDeliveryOptionShouldHaveNameAndPrice() {
        List<Map<String, Object>> options = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(options).isNotEmpty();
        var first = options.get(0);
        assertThat(first.keySet()).contains("name", "price");
        assertThat(String.valueOf(first.get("name"))).isNotBlank();
        assertThat(first.get("price")).isNotNull();
    }

    @Then("each delivery option should include estimated days range")
    public void eachDeliveryOptionShouldIncludeEstimatedDays() {
        List<Map<String, Object>> options = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(options).isNotEmpty();
        assertThat(options.get(0).keySet()).contains("estimated_days_min", "estimated_days_max");
    }
}
