package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.domain.DeliveryState;
import com.myecommerce.automation.dsl.domain.ProductDetail;
import com.myecommerce.automation.dsl.domain.ProductListing;
import com.myecommerce.automation.dsl.domain.SearchResults;

public interface CatalogueProtocol {

    void browseCatalogue();
    void viewFirstProduct();
    void searchFor(String term);
    void returnToProductListing();
    void chooseAlternativeDeliveryOption();

    ProductListing getProductListing();
    ProductDetail  getProductDetail();
    DeliveryState  getDeliveryState();
    SearchResults  getSearchResults();
    String         currentUrl();
}
