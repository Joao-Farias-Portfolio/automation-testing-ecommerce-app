package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.SavedDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SavedWishlistSteps {

    private final SavedDsl saved = new SavedDsl(DriverFactory.create());

    @Given("the shopper is on the homepage with save buttons visible")
    public void shopperOnHomepageWithSaveButtonsVisible() {
        saved.navigateHomeAndWaitForSaveButtons();
    }

    @Given("the shopper records the initial save state of the first product")
    public void shopperRecordsInitialSaveState() {
        saved.recordInitialSaveState();
    }

    @Given("the shopper has saved the first product")
    public void shopperHasSavedFirstProduct() {
        saved.ensureFirstProductIsSaved();
    }

    @Given("the shopper is on the saved page")
    public void shopperIsOnSavedPage() {
        saved.openSavedPage();
    }

    @When("the shopper toggles the save button for the first product")
    public void shopperTogglesSaveButtonForFirstProduct() {
        saved.toggleFirstSaveButton();
    }

    @When("the shopper toggles the save button again")
    public void shopperTogglesSaveButtonAgain() {
        saved.toggleFirstSaveButton();
    }

    @When("the shopper navigates to the saved page")
    public void shopperNavigatesToSavedPage() {
        saved.openSavedPage();
    }

    @When("the shopper clicks the wishlist link")
    public void shopperClicksWishlistLink() {
        saved.clickWishlistLink();
    }

    @Then("the save state of the first product should have changed")
    public void saveStateShouldHaveChanged() {
        saved.assertSaveStateChanged();
    }

    @Then("the save state should be restored to the initial state")
    public void saveStateShouldBeRestoredToInitialState() {
        saved.assertSaveStateRestored();
    }

    @Then("the saved count should be visible and show a number")
    public void savedCountShouldBeVisibleAndShowNumber() {
        saved.assertSavedCountIsVisible();
    }

    @Then("the wishlist link should be visible")
    public void wishlistLinkShouldBeVisible() {
        saved.assertWishlistLinkVisible();
    }

    @Then("the URL should contain \\/saved")
    public void urlShouldContainSaved() {
        saved.assertUrlContainsSaved();
    }

    @Then("the save button should be visible and functional on the detail page")
    public void saveButtonShouldBeVisibleAndFunctional() {
        saved.assertSaveButtonVisibleAndFunctional();
    }
}
