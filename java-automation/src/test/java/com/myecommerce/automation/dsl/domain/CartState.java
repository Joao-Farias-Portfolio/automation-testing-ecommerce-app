package com.myecommerce.automation.dsl.domain;

import java.util.List;

public record CartState(int itemCount, String total, List<CartItem> items, boolean isEmpty) {
    public boolean hasItems() {
        return itemCount > 0;
    }
}
