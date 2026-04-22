package com.lineasupply.automation.dsl;

import com.lineasupply.automation.dsl.domain.CartState;
import com.lineasupply.automation.dsl.domain.DeliveryState;
import com.lineasupply.automation.dsl.domain.ProductDetail;
import com.lineasupply.automation.dsl.domain.ProductListing;
import com.lineasupply.automation.dsl.domain.SavedState;
import com.lineasupply.automation.dsl.domain.SearchResults;

public interface LineasupplyProtocol {

    void openHomePage();
    void openCartPage();
    void openSavedPage();
    void navigateBack();

    void addProductToCart(int index);
    void removeItemFromCart(int index);
    void changeQuantityTo(int quantity);
    void searchFor(String term);
    void clickProductCard(int index);
    void selectDeliveryOption(int index);
    void toggleSaveProduct(int index);
    void clickWishlistLink();

    ProductListing getProductListing();
    CartState      getCartState();
    ProductDetail  getProductDetail();
    DeliveryState  getDeliveryState();
    SearchResults  getSearchResults();
    SavedState     getSavedState();
    String         currentUrl();
}
