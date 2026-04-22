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

    void addProductToCart();
    void removeFirstItemFromCart();
    void changeQuantityTo(int quantity);
    void searchFor(String term);
    void clickFirstProductCard();
    void selectAlternativeDeliveryOption();
    void toggleFirstSaveButton();
    void clickWishlistLink();

    ProductListing getProductListing();
    CartState      getCartState();
    ProductDetail  getProductDetail();
    DeliveryState  getDeliveryState();
    SearchResults  getSearchResults();
    SavedState     getSavedState();
    String         currentUrl();

    void waitForCartCountToBe(int expected);
    void waitForCartTotalToChange(String previousTotal);
    void waitForCartToBeEmpty();
    void waitForCartItemsToAppear();
    void waitForProductsToLoad();
    void waitForSearchResultsToLoad();
}
