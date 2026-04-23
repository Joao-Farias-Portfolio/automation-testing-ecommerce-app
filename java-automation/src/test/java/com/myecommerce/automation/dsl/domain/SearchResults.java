package com.myecommerce.automation.dsl.domain;

import java.util.List;

public record SearchResults(List<ProductCard> cards, boolean emptyStateVisible) {}
