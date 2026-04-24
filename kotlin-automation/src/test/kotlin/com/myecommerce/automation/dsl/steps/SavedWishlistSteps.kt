package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createSaved
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat

class SavedWishlistSteps {

    private val saved by lazy { createSaved() }

    private var initialSaveState: Boolean = false

    @Given("the shopper is on the homepage with save buttons visible")
    fun shopperOnHomepageWithSaveButtonsVisible() = saved.browseCatalogue()

    @Given("the shopper records the initial save state of the first product")
    fun shopperRecordsInitialSaveState() {
        initialSaveState = saved.getSavedState().saveButtonPressed
    }

    @Given("the shopper has saved the first product")
    fun shopperHasSavedFirstProduct() = saved.ensureFirstProductIsSaved()

    @Given("the shopper is on the saved page")
    fun shopperIsOnSavedPage() = saved.viewSavedItems()

    @When("the shopper toggles the save button for the first product")
    fun shopperTogglesSaveButtonForFirstProduct() = saved.toggleSaveStateOfFirstProduct()

    @When("the shopper toggles the save button again")
    fun shopperTogglesSaveButtonAgain() = saved.toggleSaveStateOfFirstProduct()

    @When("the shopper navigates to the saved page")
    fun shopperNavigatesToSavedPage() = saved.viewSavedItems()

    @When("the shopper clicks the wishlist link")
    fun shopperClicksWishlistLink() = saved.viewWishlist()

    @Then("the save state of the first product should have changed")
    fun saveStateShouldHaveChanged() {
        assertThat(saved.getSavedState().saveButtonPressed)
            .`as`("save state of first product should have changed from initial state ($initialSaveState)")
            .isNotEqualTo(initialSaveState)
    }

    @Then("the save state should be restored to the initial state")
    fun saveStateShouldBeRestoredToInitialState() {
        assertThat(saved.getSavedState().saveButtonPressed)
            .`as`("save state should be restored to initial state ($initialSaveState)")
            .isEqualTo(initialSaveState)
    }

    @Then("the saved count should be visible and show a number")
    fun savedCountShouldBeVisibleAndShowNumber() {
        assertThat(saved.getSavedState().savedPageCount)
            .`as`("saved count should be visible and show a number >= 1")
            .isGreaterThanOrEqualTo(1)
    }

    @Then("the wishlist link should be visible")
    fun wishlistLinkShouldBeVisible() {
        assertThat(saved.getSavedState().wishlistLinkVisible)
            .`as`("wishlist link should be visible on the saved page")
            .isTrue
    }

    @Then("the URL should contain \\/saved")
    fun urlShouldContainSaved() {
        assertThat(saved.currentUrl())
            .`as`("URL should contain '/saved'")
            .contains("/saved")
    }

    @Then("the save button should be visible and functional on the detail page")
    fun saveButtonShouldBeVisibleAndFunctional() {
        val state = saved.getSavedState()
        assertThat(state.saveButtonPresent).`as`("save button should be present and displayed").isTrue
        assertThat(state.saveButtonEnabled).`as`("save button should be enabled").isTrue
    }
}
