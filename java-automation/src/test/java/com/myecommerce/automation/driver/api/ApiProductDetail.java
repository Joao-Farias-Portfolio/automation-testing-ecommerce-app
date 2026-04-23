package com.myecommerce.automation.driver.api;

import java.util.List;

public record ApiProductDetail(
        int id,
        String title,
        String description,
        double price,
        String imageUrl,
        List<ApiDeliveryOption> deliveryOptions) {}
