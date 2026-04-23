package com.myecommerce.automation.driver.api;

public record ApiDeliveryOption(
        int id,
        String name,
        String description,
        double price,
        boolean isActive,
        Double minOrderAmount) {}
