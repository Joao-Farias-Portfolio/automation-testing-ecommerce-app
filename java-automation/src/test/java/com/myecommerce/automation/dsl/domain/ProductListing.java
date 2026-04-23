package com.myecommerce.automation.dsl.domain;

import java.util.List;

public record ProductListing(List<ProductCard> cards, boolean hasVisibleLoadingIndicators) {}
