package com.lineasupply.automation.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CartPage extends PageObject {

    @FindBy(css = "[data-testid='cart-item']")
    private List<WebElementFacade> cartItems;

    @FindBy(css = "[data-testid='cart-total']")
    private WebElementFacade cartTotal;

    @FindBy(css = "[data-testid='empty-cart']")
    private WebElementFacade emptyCartMessage;

    @FindBy(css = "[data-testid='remove-item']")
    private List<WebElementFacade> removeItemButtons;

    public int getCartItemCount() {
        return cartItems.size();
    }

    public boolean isCartEmpty() {
        return emptyCartMessage.isPresent() && emptyCartMessage.isVisible();
    }

    public String getCartTotalText() {
        return cartTotal.waitUntilVisible().getText();
    }

    public void removeFirstItem() {
        removeItemButtons.get(0).waitUntilClickable().click();
    }

    public boolean isCartTotalVisible() {
        return cartTotal.isPresent() && cartTotal.isVisible();
    }
}
