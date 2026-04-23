package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.domain.CartState;

public interface CartProtocol extends CatalogueProtocol {

    void viewCart();
    void addProductToCart();
    void removeFirstItemFromCart();
    void changeQuantityTo(int quantity);

    CartState getCartState();
}
