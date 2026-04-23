package com.myecommerce.automation.dsl.domain;

public record ProductDetail(
    String title,
    String price,
    String description,
    boolean imagePresent,
    String addToCartButtonText,
    boolean addToCartEnabled
) {}
