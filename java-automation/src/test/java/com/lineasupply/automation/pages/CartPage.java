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

    @FindBy(css = "[data-testid='quantity-display']")
    private List<WebElementFacade> quantityDisplays;

    @FindBy(css = "[data-testid='increment-qty']")
    private List<WebElementFacade> incrementButtons;

    @FindBy(css = "[data-testid='decrement-qty']")
    private List<WebElementFacade> decrementButtons;

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

    public void setQuantity(int quantity) {
        if (!quantityDisplays.isEmpty()) {
            String inputType = quantityDisplays.get(0).getAttribute("type");
            if ("text".equals(inputType) || "number".equals(inputType)) {
                quantityDisplays.get(0).clear();
                quantityDisplays.get(0).type(String.valueOf(quantity));
                quantityDisplays.get(0).sendKeys(org.openqa.selenium.Keys.TAB);
                return;
            }
        }
        int current = getCurrentQuantity();
        if (quantity > current) {
            for (int i = current; i < quantity; i++) {
                incrementButtons.get(0).click();
            }
        } else {
            for (int i = current; i > quantity; i--) {
                decrementButtons.get(0).click();
            }
        }
    }

    private int getCurrentQuantity() {
        if (quantityDisplays.isEmpty()) return 1;
        String text = quantityDisplays.get(0).getText();
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public boolean isCartTotalVisible() {
        return cartTotal.isPresent() && cartTotal.isVisible();
    }
}
