package com.myecommerce.automation.dsl.steps;

import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class DeliveryOptionsSteps {

    private final CatalogueProtocol catalogue = DriverFactory.createCatalogue();
    private String notedSelectedDeliveryOption;

    @Given("the shopper has navigated to a product detail page")
    public void shopperHasNavigatedToProductDetailPage() {
        catalogue.browseCatalogue();
        catalogue.viewFirstProduct();
        log.fine("navigated to first product detail page");
    }

    @When("the shopper notes the currently selected delivery option")
    public void shopperNotesCurrentlySelectedDeliveryOption() {
        notedSelectedDeliveryOption = catalogue.getDeliveryState().options().stream()
            .filter(o -> o.selected())
            .map(o -> o.label())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No delivery option is currently selected"));
        log.fine("noted selected delivery option: '%s'".formatted(notedSelectedDeliveryOption));
    }

    @When("the shopper selects a different delivery option")
    public void shopperSelectsDifferentDeliveryOption() {
        catalogue.chooseAlternativeDeliveryOption();
        log.fine("selected alternative delivery option");
    }

    @Then("the delivery section should be visible")
    public void deliverySectionShouldBeVisible() {
        assertThat(catalogue.getDeliveryState().sectionVisible())
            .as("delivery section should be visible on the product detail page")
            .isTrue();
        log.fine("delivery section is visible");
    }

    @Then("the delivery section should contain radio button options")
    public void deliverySectionShouldContainRadioButtonOptions() {
        var options = catalogue.getDeliveryState().options();
        assertThat(options)
            .as("delivery section should contain radio button options")
            .isNotEmpty();
        log.fine("delivery section has %d radio options".formatted(options.size()));
    }

    @Then("one delivery option should be selected by default")
    public void oneDeliveryOptionShouldBeSelectedByDefault() {
        var state = catalogue.getDeliveryState();
        assertThat(state.sectionVisible())
            .as("delivery section must be visible")
            .isTrue();
        assertThat(state.selectedOptionCount())
            .as("exactly one delivery option should be selected by default")
            .isEqualTo(1L);
        log.fine("exactly one delivery option is selected by default");
    }

    @Then("a different delivery option should now be selected")
    public void differentDeliveryOptionShouldBeSelected() {
        var state = catalogue.getDeliveryState();
        assertThat(state.sectionVisible())
            .as("delivery section must be visible")
            .isTrue();
        assertThat(state.selectedOptionCount())
            .as("exactly one delivery option should be selected after changing")
            .isEqualTo(1L);
        String nowSelected = state.options().stream()
            .filter(o -> o.selected())
            .map(o -> o.label())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No delivery option is selected after changing"));
        assertThat(nowSelected)
            .as("selected delivery option should have changed from '%s'".formatted(notedSelectedDeliveryOption))
            .isNotEqualTo(notedSelectedDeliveryOption);
        log.fine("delivery option changed from '%s' to '%s'".formatted(notedSelectedDeliveryOption, nowSelected));
    }

    @Then("no minimum order restrictions should be shown")
    public void noMinimumOrderRestrictionsShouldBeShown() {
        assertThat(catalogue.getDeliveryState().minimumOrderTextPresent())
            .as("no minimum order restrictions should be shown")
            .isFalse();
        log.fine("no minimum order restrictions are shown");
    }

    @Then("the delivery section should have a header with delivery options text")
    public void deliverySectionShouldHaveHeader() {
        assertThat(catalogue.getDeliveryState().headerText())
            .as("delivery section header should contain 'delivery options'")
            .isNotBlank();
        log.fine("delivery section header check passed");
    }

    @Then("the product detail page should still be functional without delivery options")
    public void productDetailPageShouldStillBeFunctionalWithoutDeliveryOptions() {
        var detail = catalogue.getProductDetail();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(detail.title()).as("product title should be present").isNotBlank();
        soft.assertThat(detail.price()).as("product price should be present").isNotBlank();
        soft.assertThat(detail.addToCartButtonText()).as("add-to-cart button should be present").isNotBlank();
        soft.assertAll();
        log.fine("product detail page is functional: title, price, and add-to-cart button present");
    }
}
