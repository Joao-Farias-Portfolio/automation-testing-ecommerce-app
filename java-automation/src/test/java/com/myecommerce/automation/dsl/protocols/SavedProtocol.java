package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.domain.SavedState;

public interface SavedProtocol extends CatalogueProtocol {

    void viewSavedItems();
    void ensureFirstProductIsSaved();
    void toggleSaveStateOfFirstProduct();
    void viewWishlist();

    SavedState getSavedState();
}
