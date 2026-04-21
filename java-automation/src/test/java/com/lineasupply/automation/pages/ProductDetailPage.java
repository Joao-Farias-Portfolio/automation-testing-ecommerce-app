package com.lineasupply.automation.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ProductDetailPage extends PageObject {

    @FindBy(css = "[data-testid='product-title']")
    private WebElementFacade productTitle;

    @FindBy(css = "[data-testid='product-price']")
    private WebElementFacade productPrice;

    @FindBy(css = "[data-testid='product-description']")
    private WebElementFacade productDescription;

    @FindBy(css = "[data-testid='product-detail-image']")
    private WebElementFacade productImage;

    @FindBy(css = "[data-testid='add-to-cart']")
    private WebElementFacade addToCartButton;

    @FindBy(css = "[data-testid='cart-count']")
    private WebElementFacade cartCountBadge;

    @FindBy(css = "[data-testid='save-button']")
    private WebElementFacade saveButton;

    @FindBy(css = "[data-testid='delivery-section']")
    private WebElementFacade deliverySection;

    @FindBy(css = "nav[aria-label='breadcrumb']")
    private WebElementFacade breadcrumb;

    public String getProductTitle() {
        return productTitle.waitUntilVisible().getText();
    }

    public String getProductPrice() {
        return productPrice.waitUntilVisible().getText();
    }

    public String getProductDescription() {
        return productDescription.waitUntilVisible().getText();
    }

    public boolean isProductImageVisible() {
        return productImage.isPresent() && productImage.isVisible();
    }

    public void addToCart() {
        addToCartButton.waitUntilClickable().click();
    }

    public boolean isAddToCartDisabled() {
        return addToCartButton.isDisabled();
    }

    public String getAddToCartButtonText() {
        return addToCartButton.getText();
    }

    public int getCartCount() {
        if (!cartCountBadge.isPresent() || !cartCountBadge.isVisible()) return 0;
        String text = cartCountBadge.getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public boolean isDeliverySectionPresent() {
        return deliverySection.isPresent();
    }

    public boolean isBreadcrumbVisible() {
        return breadcrumb.isPresent() && breadcrumb.isVisible();
    }

    public void toggleSave() {
        saveButton.waitUntilClickable().click();
    }

    public String getSaveButtonState() {
        return saveButton.getAttribute("aria-pressed");
    }

    public List<WebElementFacade> getDeliveryRadioButtons() {
        return deliverySection.thenFindAll("input[type='radio']");
    }
}
