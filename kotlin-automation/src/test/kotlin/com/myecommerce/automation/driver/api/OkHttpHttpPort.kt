package com.myecommerce.automation.driver.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.myecommerce.automation.driver.ports.HttpPort
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpHttpPort(private val baseUrl: String) : HttpPort {

    private val client = OkHttpClient()
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    override fun <T : Any> getAs(path: String, type: Class<T>): T =
        mapper.readValue(get(path), type)

    override fun <T : Any> getListAs(path: String, elementType: Class<T>): List<T> =
        deserializeList(get(path), elementType)

    override fun <T : Any> getListWithQueryAs(path: String, paramName: String, paramValue: String, elementType: Class<T>): List<T> =
        deserializeList(get(path, paramName to paramValue), elementType)

    private fun get(path: String, query: Pair<String, String>? = null): String {
        val url = "$baseUrl$path".toHttpUrl().newBuilder()
            .apply { query?.let { addQueryParameter(it.first, it.second) } }
            .build()
        val response = client.newCall(Request.Builder().url(url).build()).execute()
        check(response.code == 200) { "Expected 200 but got ${response.code} for $path" }
        return response.body?.string() ?: ""
    }

    private fun <T : Any> deserializeList(json: String, elementType: Class<T>): List<T> {
        val listType = mapper.typeFactory.constructCollectionType(List::class.java, elementType)
        return mapper.readValue(json, listType)
    }
}
