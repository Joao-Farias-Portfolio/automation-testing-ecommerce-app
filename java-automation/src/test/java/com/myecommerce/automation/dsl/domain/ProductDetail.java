package com.myecommerce.automation.dsl.domain;

import lombok.Builder;

@Builder
public record ProductDetail(
    String title,
    String price,
    String description,
    boolean imagePresent,
    String addToCartButtonText,
    boolean addToCartEnabled
) {}
