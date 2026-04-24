package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createCatalogue
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions

class DeliveryOptionsSteps {

    private val catalogue = createCatalogue()
    private var notedSelectedDeliveryOption: String = ""

    @Given("the shopper has navigated to a product detail page")
    fun shopperHasNavigatedToProductDetailPage() {
        catalogue.browseCatalogue()
        catalogue.viewFirstProduct()
    }

    @When("the shopper notes the currently selected delivery option")
    fun shopperNotesCurrentlySelectedDeliveryOption() {
        notedSelectedDeliveryOption = catalogue.getDeliveryState().options
            .firstOrNull { it.selected }?.label
            ?: error("No delivery option is currently selected")
    }

    @When("the shopper selects a different delivery option")
    fun shopperSelectsDifferentDeliveryOption() = catalogue.chooseAlternativeDeliveryOption()

    @Then("the delivery section should be visible")
    fun deliverySectionShouldBeVisible() {
        assertThat(catalogue.getDeliveryState().sectionVisible)
            .`as`("delivery section should be visible on the product detail page")
            .isTrue
    }

    @Then("the delivery section should contain radio button options")
    fun deliverySectionShouldContainRadioButtonOptions() {
        assertThat(catalogue.getDeliveryState().options)
            .`as`("delivery section should contain radio button options")
            .isNotEmpty
    }

    @Then("one delivery option should be selected by default")
    fun oneDeliveryOptionShouldBeSelectedByDefault() {
        val state = catalogue.getDeliveryState()
        assertThat(state.sectionVisible).`as`("delivery section must be visible").isTrue
        assertThat(state.selectedOptionCount())
            .`as`("exactly one delivery option should be selected by default")
            .isEqualTo(1L)
    }

    @Then("a different delivery option should now be selected")
    fun differentDeliveryOptionShouldBeSelected() {
        val state = catalogue.getDeliveryState()
        assertThat(state.sectionVisible).`as`("delivery section must be visible").isTrue
        assertThat(state.selectedOptionCount())
            .`as`("exactly one delivery option should be selected after changing")
            .isEqualTo(1L)
        val nowSelected = state.options.firstOrNull { it.selected }?.label
            ?: error("No delivery option is selected after changing")
        assertThat(nowSelected)
            .`as`("selected delivery option should have changed from '$notedSelectedDeliveryOption'")
            .isNotEqualTo(notedSelectedDeliveryOption)
    }

    @Then("no minimum order restrictions should be shown")
    fun noMinimumOrderRestrictionsShouldBeShown() {
        assertThat(catalogue.getDeliveryState().minimumOrderTextPresent)
            .`as`("no minimum order restrictions should be shown")
            .isFalse
    }

    @Then("the delivery section should have a header with delivery options text")
    fun deliverySectionShouldHaveHeader() {
        assertThat(catalogue.getDeliveryState().headerText)
            .`as`("delivery section header should contain 'delivery options'")
            .isNotBlank
    }

    @Then("the product detail page should still be functional without delivery options")
    fun productDetailPageShouldStillBeFunctionalWithoutDeliveryOptions() {
        val detail = catalogue.getProductDetail()
        SoftAssertions().apply {
            assertThat(detail.title).`as`("product title should be present").isNotBlank
            assertThat(detail.price).`as`("product price should be present").isNotBlank
            assertThat(detail.addToCartButtonText).`as`("add-to-cart button should be present").isNotBlank
            assertAll()
        }
    }
}
