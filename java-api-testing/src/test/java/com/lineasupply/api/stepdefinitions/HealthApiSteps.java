package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthApiSteps {

    private static final String BASE_URL = "http://localhost:8001";

    @When("the API consumer checks the health endpoint")
    public void checkHealthEndpoint() {
        SerenityRest.given()
            .baseUri(BASE_URL)
            .when()
            .get("/health");
    }

    @Then("the status code should be {int}")
    public void statusCodeShouldBe(int expectedStatus) {
        assertThat(SerenityRest.lastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response body should contain {string}")
    public void responseBodyShouldContain(String text) {
        assertThat(SerenityRest.lastResponse().body().asString()).containsIgnoringCase(text);
    }
}
