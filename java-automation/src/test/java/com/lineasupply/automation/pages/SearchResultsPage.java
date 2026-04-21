package com.lineasupply.automation.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SearchResultsPage extends PageObject {

    @FindBy(css = "[data-testid='product-card']")
    private List<WebElementFacade> productCards;

    @FindBy(css = "[data-testid='no-results']")
    private WebElementFacade noResultsMessage;

    public boolean hasProducts() {
        return !productCards.isEmpty();
    }

    public int getProductCount() {
        return productCards.size();
    }

    public boolean isNoResultsVisible() {
        return noResultsMessage.isPresent() && noResultsMessage.isVisible();
    }

    public void waitForResults() {
        withTimeoutOf(10, java.util.concurrent.TimeUnit.SECONDS)
            .waitFor("[data-testid='product-card'], [data-testid='no-results']");
    }
}
