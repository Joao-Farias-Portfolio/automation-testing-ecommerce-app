package com.lineasupply.api.stepdefinitions;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

import static org.assertj.core.api.Assertions.assertThat;

public class Hooks {

    private static final String BASE_URL = "http://localhost:8001";

    @Before(order = 1)
    public void verifyBackendReady() {
        var response = RestAssured.given()
                .baseUri(BASE_URL)
                .when()
                .get("/health");
        assertThat(response.statusCode())
                .as("Backend must be running on port 8001 before API tests execute")
                .isEqualTo(200);
    }
}
