package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.domain.CartState;
import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SavedState;
import com.myecommerce.automation.dsl.domain.SearchResults;

public interface MyEcommerceProtocol {

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
    void waitForSavedPageToLoad();
}
