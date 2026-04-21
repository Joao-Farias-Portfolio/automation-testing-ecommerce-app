package com.lineasupply.automation.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SavedPage extends PageObject {

    @FindBy(css = "[data-testid='product-card']")
    private List<WebElementFacade> savedProductCards;

    @FindBy(css = "[data-testid='saved-count']")
    private WebElementFacade savedCount;

    @FindBy(css = "[data-testid='wishlist-link']")
    private WebElementFacade wishlistLink;

    public int getSavedCount() {
        String text = savedCount.waitUntilVisible().getText().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isSavedCountVisible() {
        return savedCount.isPresent() && savedCount.isVisible();
    }

    public boolean hasProducts() {
        return !savedProductCards.isEmpty();
    }

    public boolean isWishlistLinkVisible() {
        return wishlistLink.isPresent() && wishlistLink.isVisible();
    }

    public void clickWishlistLink() {
        wishlistLink.waitUntilClickable().click();
    }
}
