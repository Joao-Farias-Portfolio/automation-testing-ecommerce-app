package com.lineasupply.automation.driver.web;

import com.lineasupply.automation.dsl.LineasupplyProtocol;
import com.lineasupply.automation.dsl.domain.CartState;
import com.lineasupply.automation.dsl.domain.DeliveryState;
import com.lineasupply.automation.dsl.domain.ProductDetail;
import com.lineasupply.automation.dsl.domain.ProductListing;
import com.lineasupply.automation.dsl.domain.SavedState;
import com.lineasupply.automation.dsl.domain.SearchResults;

import java.util.logging.Logger;

public class LineasupplyDriver implements LineasupplyProtocol {

    private static final Logger log = Logger.getLogger(LineasupplyDriver.class.getName());

    @Override
    public void openHomePage() {
        throw new UnsupportedOperationException("openHomePage not yet implemented");
    }

    @Override
    public void openCartPage() {
        throw new UnsupportedOperationException("openCartPage not yet implemented");
    }

    @Override
    public void openSavedPage() {
        throw new UnsupportedOperationException("openSavedPage not yet implemented");
    }

    @Override
    public void navigateBack() {
        throw new UnsupportedOperationException("navigateBack not yet implemented");
    }

    @Override
    public void addProductToCart(int index) {
        throw new UnsupportedOperationException("addProductToCart not yet implemented");
    }

    @Override
    public void removeItemFromCart(int index) {
        throw new UnsupportedOperationException("removeItemFromCart not yet implemented");
    }

    @Override
    public void changeQuantityTo(int quantity) {
        throw new UnsupportedOperationException("changeQuantityTo not yet implemented");
    }

    @Override
    public void searchFor(String term) {
        throw new UnsupportedOperationException("searchFor not yet implemented");
    }

    @Override
    public void clickProductCard(int index) {
        throw new UnsupportedOperationException("clickProductCard not yet implemented");
    }

    @Override
    public void selectDeliveryOption(int index) {
        throw new UnsupportedOperationException("selectDeliveryOption not yet implemented");
    }

    @Override
    public void toggleSaveProduct(int index) {
        throw new UnsupportedOperationException("toggleSaveProduct not yet implemented");
    }

    @Override
    public void clickWishlistLink() {
        throw new UnsupportedOperationException("clickWishlistLink not yet implemented");
    }

    @Override
    public ProductListing getProductListing() {
        throw new UnsupportedOperationException("getProductListing not yet implemented");
    }

    @Override
    public CartState getCartState() {
        throw new UnsupportedOperationException("getCartState not yet implemented");
    }

    @Override
    public ProductDetail getProductDetail() {
        throw new UnsupportedOperationException("getProductDetail not yet implemented");
    }

    @Override
    public DeliveryState getDeliveryState() {
        throw new UnsupportedOperationException("getDeliveryState not yet implemented");
    }

    @Override
    public SearchResults getSearchResults() {
        throw new UnsupportedOperationException("getSearchResults not yet implemented");
    }

    @Override
    public SavedState getSavedState() {
        throw new UnsupportedOperationException("getSavedState not yet implemented");
    }

    @Override
    public String currentUrl() {
        throw new UnsupportedOperationException("currentUrl not yet implemented");
    }
}
