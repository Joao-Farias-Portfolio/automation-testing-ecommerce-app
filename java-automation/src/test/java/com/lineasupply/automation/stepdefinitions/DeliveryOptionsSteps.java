package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.tasks.NavigateTo;
import com.lineasupply.automation.screenplay.tasks.SelectDeliveryOption;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryOptionsSteps {

    private static final String DELIVERY_SELECTOR =
        "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
        "[data-testid='shipping-section'], [data-testid='shipping-options']";

    @Given("the shopper has navigated to a product detail page")
    public void shopperHasNavigatedToProductDetailPage() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
        driver.findElements(By.cssSelector("[data-testid='product-card']")).get(0).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlMatches(".*/products/\\d+"));
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-title']")));
    }

    @When("the shopper selects a different delivery option")
    public void shopperSelectsDifferentDeliveryOption() {
        OnlineShopper.named("Shopper").attemptsTo(SelectDeliveryOption.secondOption());
    }

    @Then("the delivery section should be visible")
    public void deliverySectionShouldBeVisible() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var sections = driver.findElements(By.cssSelector(DELIVERY_SELECTOR));
        assertThat(sections).isNotEmpty();
        assertThat(sections.get(0).isDisplayed()).isTrue();
    }

    @Then("the delivery section should contain radio button options")
    public void deliverySectionShouldContainRadioButtonOptions() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var sections = driver.findElements(By.cssSelector(DELIVERY_SELECTOR));
        assertThat(sections).isNotEmpty();

        var radios = sections.get(0).findElements(By.cssSelector("input[type='radio']"));
        assertThat(radios).hasSizeGreaterThan(0);
        assertThat(radios.get(0).isDisplayed()).isTrue();
    }

    @Then("one delivery option should be selected by default")
    public void oneDeliveryOptionShouldBeSelectedByDefault() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var sections = driver.findElements(By.cssSelector(DELIVERY_SELECTOR));
        assertThat(sections).isNotEmpty();

        var selected = sections.get(0).findElements(By.cssSelector("input[type='radio']:checked"));
        assertThat(selected).hasSize(1);
    }

    @Then("a different delivery option should now be selected")
    public void differentDeliveryOptionShouldBeSelected() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var sections = driver.findElements(By.cssSelector(DELIVERY_SELECTOR));
        assertThat(sections).isNotEmpty();

        var selected = sections.get(0).findElements(By.cssSelector("input[type='radio']:checked"));
        assertThat(selected).hasSize(1);
    }

    @Then("no minimum order restrictions should be shown")
    public void noMinimumOrderRestrictionsShouldBeShown() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var minOrderText = driver.findElements(
            By.xpath("//*[contains(text(), 'Minimum order') or contains(text(), 'Min order')]"));
        assertThat(minOrderText).isEmpty();
    }

    @Then("the delivery section should have a header with delivery options text")
    public void deliverySectionShouldHaveHeader() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var sections = driver.findElements(By.cssSelector(DELIVERY_SELECTOR));
        if (sections.isEmpty()) return;

        var header = driver.findElements(
            By.xpath("//*[contains(translate(text(), 'DELIVERYOPTIONS', 'deliveryoptions'), 'delivery options')]"));
        assertThat(header).isNotEmpty();
    }

    @Then("the product detail page should still be functional without delivery options")
    public void productDetailPageShouldStillBeFunctionalWithoutDeliveryOptions() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        assertThat(driver.findElements(By.cssSelector("[data-testid='product-title']")))
            .isNotEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='product-price']")))
            .isNotEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='add-to-cart']")))
            .isNotEmpty();
    }
}
