package com.myecommerce.automation.dsl.steps;

import com.myecommerce.automation.dsl.protocols.DriverFactory;
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class SavedWishlistSteps {

    private final MyEcommerceProtocol protocol = DriverFactory.create();
    private boolean initialSaveState;

    @Given("the shopper is on the homepage with save buttons visible")
    public void shopperOnHomepageWithSaveButtonsVisible() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("navigated to homepage and waited for save buttons");
    }

    @Given("the shopper records the initial save state of the first product")
    public void shopperRecordsInitialSaveState() {
        initialSaveState = protocol.getSavedState().saveButtonPressed();
        log.fine("recorded initial save state: pressed=" + initialSaveState);
    }

    @Given("the shopper has saved the first product")
    public void shopperHasSavedFirstProduct() {
        if (!protocol.getSavedState().saveButtonPressed()) {
            protocol.toggleFirstSaveButton();
            log.fine("toggled save button to saved state");
        } else {
            log.fine("first product already saved; no toggle needed");
        }
    }

    @Given("the shopper is on the saved page")
    public void shopperIsOnSavedPage() {
        protocol.openSavedPage();
        protocol.waitForSavedPageToLoad();
        log.fine("navigated to saved page");
    }

    @When("the shopper toggles the save button for the first product")
    public void shopperTogglesSaveButtonForFirstProduct() {
        protocol.toggleFirstSaveButton();
        log.fine("toggled first save button");
    }

    @When("the shopper toggles the save button again")
    public void shopperTogglesSaveButtonAgain() {
        protocol.toggleFirstSaveButton();
        log.fine("toggled first save button");
    }

    @When("the shopper navigates to the saved page")
    public void shopperNavigatesToSavedPage() {
        protocol.openSavedPage();
        protocol.waitForSavedPageToLoad();
        log.fine("navigated to saved page");
    }

    @When("the shopper clicks the wishlist link")
    public void shopperClicksWishlistLink() {
        protocol.clickWishlistLink();
        log.fine("clicked wishlist link");
    }

    @Then("the save state of the first product should have changed")
    public void saveStateShouldHaveChanged() {
        boolean currentState = protocol.getSavedState().saveButtonPressed();
        assertThat(currentState)
            .as("save state of first product should have changed from initial state (" + initialSaveState + ")")
            .isNotEqualTo(initialSaveState);
        log.fine("save state changed from " + initialSaveState + " to " + currentState);
    }

    @Then("the save state should be restored to the initial state")
    public void saveStateShouldBeRestoredToInitialState() {
        boolean currentState = protocol.getSavedState().saveButtonPressed();
        assertThat(currentState)
            .as("save state should be restored to initial state (" + initialSaveState + ")")
            .isEqualTo(initialSaveState);
        log.fine("save state restored to initial: " + currentState);
    }

    @Then("the saved count should be visible and show a number")
    public void savedCountShouldBeVisibleAndShowNumber() {
        int count = protocol.getSavedState().savedPageCount();
        assertThat(count)
            .as("saved count should be visible and show a number >= 1")
            .isGreaterThanOrEqualTo(1);
        log.fine("saved count is visible: " + count);
    }

    @Then("the wishlist link should be visible")
    public void wishlistLinkShouldBeVisible() {
        assertThat(protocol.getSavedState().wishlistLinkVisible())
            .as("wishlist link should be visible on the saved page")
            .isTrue();
        log.fine("wishlist link is visible");
    }

    @Then("the URL should contain \\/saved")
    public void urlShouldContainSaved() {
        assertThat(protocol.currentUrl())
            .as("URL should contain '/saved'")
            .contains("/saved");
        log.fine("URL contains /saved");
    }

    @Then("the save button should be visible and functional on the detail page")
    public void saveButtonShouldBeVisibleAndFunctional() {
        var state = protocol.getSavedState();
        assertThat(state.saveButtonPresent())
            .as("save button should be present and displayed")
            .isTrue();
        assertThat(state.saveButtonEnabled())
            .as("save button should be enabled")
            .isTrue();
        log.fine("save button is present and enabled");
    }
}
