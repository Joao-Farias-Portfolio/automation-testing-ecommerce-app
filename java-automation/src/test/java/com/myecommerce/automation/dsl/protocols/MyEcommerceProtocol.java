package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.domain.CartState;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SavedState;
import com.myecommerce.automation.dsl.domain.SearchResults;

public interface MyEcommerceProtocol {

    void browseCatalogue();
    void viewCart();
    void viewSavedItems();
    void returnToProductListing();

    void addProductToCart();
    void removeFirstItemFromCart();
    void changeQuantityTo(int quantity);
    void searchFor(String term);
    void viewFirstProduct();
    void chooseAlternativeDeliveryOption();
    void ensureFirstProductIsSaved();
    void toggleSaveStateOfFirstProduct();
    void viewWishlist();

    ProductListing getProductListing();
    CartState      getCartState();
    ProductDetail  getProductDetail();
    DeliveryState  getDeliveryState();
    SearchResults  getSearchResults();
    SavedState     getSavedState();
    String         currentUrl();
}
