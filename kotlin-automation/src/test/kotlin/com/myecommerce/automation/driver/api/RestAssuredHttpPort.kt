package com.myecommerce.automation.driver.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.myecommerce.automation.driver.ports.HttpPort
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import net.serenitybdd.rest.SerenityRest

class RestAssuredHttpPort(private val baseUrl: String) : HttpPort {

    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    init {
        RestAssured.config = RestAssured.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig()
                .jackson2ObjectMapperFactory { _, _ -> mapper }
        )
    }

    override fun <T : Any> getAs(path: String, type: Class<T>): T =
        SerenityRest.given().baseUri(baseUrl)
            .get(path)
            .then().statusCode(200)
            .extract().`as`(type)

    override fun <T : Any> getListAs(path: String, elementType: Class<T>): List<T> {
        val json = SerenityRest.given().baseUri(baseUrl)
            .get(path)
            .then().statusCode(200)
            .extract().asString()
        return deserializeList(json, elementType)
    }

    override fun <T : Any> getListWithQueryAs(path: String, paramName: String, paramValue: String, elementType: Class<T>): List<T> {
        val json = SerenityRest.given().baseUri(baseUrl)
            .queryParam(paramName, paramValue)
            .get(path)
            .then().statusCode(200)
            .extract().asString()
        return deserializeList(json, elementType)
    }

    private fun <T : Any> deserializeList(json: String, elementType: Class<T>): List<T> {
        val listType = mapper.typeFactory.constructCollectionType(List::class.java, elementType)
        return mapper.readValue(json, listType)
    }
}
