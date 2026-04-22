package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.DeliveryDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DeliveryOptionsSteps {

    private final DeliveryDsl delivery = new DeliveryDsl(DriverFactory.create());

    @Given("the shopper has navigated to a product detail page")
    public void shopperHasNavigatedToProductDetailPage() {
        delivery.navigateToFirstProductDetailPage();
    }

    @When("the shopper selects a different delivery option")
    public void shopperSelectsDifferentDeliveryOption() {
        delivery.selectAlternativeOption();
    }

    @Then("the delivery section should be visible")
    public void deliverySectionShouldBeVisible() {
        delivery.assertDeliverySectionVisible();
    }

    @Then("the delivery section should contain radio button options")
    public void deliverySectionShouldContainRadioButtonOptions() {
        delivery.assertDeliverySectionHasRadioOptions();
    }

    @Then("one delivery option should be selected by default")
    public void oneDeliveryOptionShouldBeSelectedByDefault() {
        delivery.assertOneOptionSelectedByDefault();
    }

    @Then("a different delivery option should now be selected")
    public void differentDeliveryOptionShouldBeSelected() {
        delivery.assertAlternativeOptionIsSelected();
    }

    @Then("no minimum order restrictions should be shown")
    public void noMinimumOrderRestrictionsShouldBeShown() {
        delivery.assertNoMinimumOrderRestrictions();
    }

    @Then("the delivery section should have a header with delivery options text")
    public void deliverySectionShouldHaveHeader() {
        delivery.assertDeliverySectionHasHeader();
    }

    @Then("the product detail page should still be functional without delivery options")
    public void productDetailPageShouldStillBeFunctionalWithoutDeliveryOptions() {
        delivery.assertProductDetailPageFunctional();
    }
}
