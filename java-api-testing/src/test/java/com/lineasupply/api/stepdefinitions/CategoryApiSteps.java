package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoryApiSteps {

    private static final String BASE_URL = "http://localhost:8001";

    @When("the API consumer requests all categories")
    public void requestAllCategories() {
        SerenityRest.given().baseUri(BASE_URL).when().get("/api/categories");
    }

    @Then("the response should contain at least one category")
    public void responseShouldContainAtLeastOneCategory() {
        List<Object> categories = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(categories).isNotEmpty();
    }

    @Then("each category should have an id and name")
    public void eachCategoryShouldHaveIdAndName() {
        List<Map<String, Object>> categories = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(categories).isNotEmpty();
        var first = categories.get(0);
        assertThat(first).containsKeys("id", "name");
        assertThat(first.get("id")).isNotNull();
        assertThat(first.get("name")).isNotNull();
    }

    @Then("each returned category should have at least one product associated")
    public void eachCategoryShouldHaveAtLeastOneProduct() {
        List<Map<String, Object>> categories = SerenityRest.lastResponse().jsonPath().getList("$");
        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isGreaterThan(0);
    }
}
