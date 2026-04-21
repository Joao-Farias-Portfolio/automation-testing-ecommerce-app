package com.lineasupply.automation.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HomePage extends PageObject {

    @FindBy(css = "[data-testid='product-card']")
    private List<WebElementFacade> productCards;

    @FindBy(css = "[data-testid='cart-count']")
    private WebElementFacade cartCountBadge;

    @FindBy(css = "[data-testid='add-to-cart']")
    private List<WebElementFacade> addToCartButtons;

    @FindBy(css = "input[placeholder*='Search Items']")
    private WebElementFacade searchInput;

    @FindBy(css = "[data-testid='cart-link']")
    private WebElementFacade cartLink;

    @FindBy(css = "[data-testid='save-button']")
    private List<WebElementFacade> saveButtons;

    @FindBy(css = "[data-testid='loading']")
    private WebElementFacade loadingIndicator;

    public void waitForProductsLoaded() {
        productCards.get(0).waitUntilVisible();
    }

    public int getCartCount() {
        if (!cartCountBadge.isPresent() || !cartCountBadge.isVisible()) return 0;
        String text = cartCountBadge.getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public void addFirstProductToCart() {
        addToCartButtons.get(0).waitUntilClickable().click();
    }

    public void addProductToCart(int index) {
        addToCartButtons.get(index).waitUntilClickable().click();
    }

    public void searchFor(String term) {
        searchInput.waitUntilVisible().type(term);
        searchInput.sendKeys(org.openqa.selenium.Keys.ENTER);
    }

    public void clickCartLink() {
        cartLink.waitUntilClickable().click();
    }

    public String getFirstProductTitle() {
        return productCards.get(0)
            .findBy("[data-testid='product-title']")
            .getText();
    }

    public void clickFirstProductCard() {
        productCards.get(0).waitUntilClickable().click();
    }

    public void clickProductCard(int index) {
        productCards.get(index).waitUntilClickable().click();
    }

    public int getProductCardCount() {
        return productCards.size();
    }

    public String getSaveButtonState(int index) {
        return saveButtons.get(index).getAttribute("aria-pressed");
    }

    public void toggleSaveForProduct(int index) {
        saveButtons.get(index).waitUntilClickable().click();
    }

    public boolean isLoadingVisible() {
        return loadingIndicator.isPresent() && loadingIndicator.isVisible();
    }
}
