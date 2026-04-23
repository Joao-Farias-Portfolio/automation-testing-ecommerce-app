package com.myecommerce.automation.driver.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.myecommerce.automation.driver.ports.HttpPort;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public final class OkHttpHttpPort implements HttpPort {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl;

    public OkHttpHttpPort(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public <T> T getAs(String path, Class<T> type) {
        String json = get(path, null, null);
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize " + path, e);
        }
    }

    @Override
    public <T> List<T> getListAs(String path, Class<T> elementType) {
        return deserializeList(get(path, null, null), elementType);
    }

    @Override
    public <T> List<T> getListWithQueryAs(String path, String paramName, String paramValue, Class<T> elementType) {
        return deserializeList(get(path, paramName, paramValue), elementType);
    }

    private String get(String path, String paramName, String paramValue) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (paramName != null) urlBuilder.addQueryParameter(paramName, paramValue);
        Request request = new Request.Builder().url(urlBuilder.build()).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new RuntimeException("Expected 200 but got " + response.code() + " for " + path);
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: " + path, e);
        }
    }

    private <T> List<T> deserializeList(String json, Class<T> elementType) {
        var listType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementType);
        try {
            return MAPPER.readValue(json, listType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize list", e);
        }
    }
}
