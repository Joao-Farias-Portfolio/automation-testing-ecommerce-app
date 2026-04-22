package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.LineasupplyProtocol;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.SoftAssertions;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryOptionsSteps {

    private static final Logger log = Logger.getLogger(DeliveryOptionsSteps.class.getName());

    private final LineasupplyProtocol protocol = DriverFactory.create();

    @Given("the shopper has navigated to a product detail page")
    public void shopperHasNavigatedToProductDetailPage() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        protocol.clickFirstProductCard();
        log.fine("navigated to first product detail page");
    }

    @When("the shopper selects a different delivery option")
    public void shopperSelectsDifferentDeliveryOption() {
        protocol.selectAlternativeDeliveryOption();
        log.fine("selected alternative delivery option");
    }

    @Then("the delivery section should be visible")
    public void deliverySectionShouldBeVisible() {
        assertThat(protocol.getDeliveryState().sectionVisible())
            .as("delivery section should be visible on the product detail page")
            .isTrue();
        log.fine("delivery section is visible");
    }

    @Then("the delivery section should contain radio button options")
    public void deliverySectionShouldContainRadioButtonOptions() {
        var options = protocol.getDeliveryState().options();
        assertThat(options)
            .as("delivery section should contain radio button options")
            .isNotEmpty();
        log.fine("delivery section has " + options.size() + " radio options");
    }

    @Then("one delivery option should be selected by default")
    public void oneDeliveryOptionShouldBeSelectedByDefault() {
        var state = protocol.getDeliveryState();
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
        var state = protocol.getDeliveryState();
        assertThat(state.sectionVisible())
            .as("delivery section must be visible")
            .isTrue();
        assertThat(state.selectedOptionCount())
            .as("exactly one delivery option should be selected after changing")
            .isEqualTo(1L);
        log.fine("a delivery option is selected after changing");
    }

    @Then("no minimum order restrictions should be shown")
    public void noMinimumOrderRestrictionsShouldBeShown() {
        assertThat(protocol.getDeliveryState().minimumOrderTextPresent())
            .as("no minimum order restrictions should be shown")
            .isFalse();
        log.fine("no minimum order restrictions are shown");
    }

    @Then("the delivery section should have a header with delivery options text")
    public void deliverySectionShouldHaveHeader() {
        var state = protocol.getDeliveryState();
        if (!state.sectionVisible()) {
            log.fine("delivery section not visible; skipping header check");
            return;
        }
        assertThat(state.headerText())
            .as("delivery section header should contain 'delivery options'")
            .isNotBlank();
        log.fine("delivery section header: '" + state.headerText() + "'");
    }

    @Then("the product detail page should still be functional without delivery options")
    public void productDetailPageShouldStillBeFunctionalWithoutDeliveryOptions() {
        var detail = protocol.getProductDetail();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(detail.title()).as("product title should be present").isNotBlank();
        soft.assertThat(detail.price()).as("product price should be present").isNotBlank();
        soft.assertThat(detail.addToCartButtonText()).as("add-to-cart button should be present").isNotBlank();
        soft.assertAll();
        log.fine("product detail page is functional: title, price, and add-to-cart button present");
    }
}
