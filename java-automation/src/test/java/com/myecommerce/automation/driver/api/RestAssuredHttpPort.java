package com.myecommerce.automation.driver.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.myecommerce.automation.driver.ports.HttpPort;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;

public final class RestAssuredHttpPort implements HttpPort {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final String baseUrl;

    public RestAssuredHttpPort(String baseUrl) {
        this.baseUrl = baseUrl;
        RestAssured.config = RestAssured.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig()
                .jackson2ObjectMapperFactory((cls, charset) -> MAPPER));
    }

    @Override
    public <T> T getAs(String path, Class<T> type) {
        return SerenityRest.given().baseUri(baseUrl)
            .get(path)
            .then().statusCode(200)
            .extract().as(type);
    }

    @Override
    public <T> List<T> getListAs(String path, Class<T> elementType) {
        var listType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementType);
        String json = SerenityRest.given().baseUri(baseUrl)
            .get(path)
            .then().statusCode(200)
            .extract().asString();
        return deserializeList(json, listType);
    }

    @Override
    public <T> List<T> getListWithQueryAs(String path, String paramName, String paramValue, Class<T> elementType) {
        var listType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementType);
        String json = SerenityRest.given().baseUri(baseUrl)
            .queryParam(paramName, paramValue)
            .get(path)
            .then().statusCode(200)
            .extract().asString();
        return deserializeList(json, listType);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> deserializeList(String json, com.fasterxml.jackson.databind.type.CollectionType type) {
        try {
            return (List<T>) MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize list response", e);
        }
    }
}
