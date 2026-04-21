package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.questions.SaveButtonState;
import com.lineasupply.automation.screenplay.tasks.NavigateTo;
import com.lineasupply.automation.screenplay.tasks.ToggleSaveProduct;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class SavedWishlistSteps {

    private String initialSaveState;

    @Given("the shopper is on the homepage with save buttons visible")
    public void shopperOnHomepageWithSaveButtonsVisible() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @Given("the shopper records the initial save state of the first product")
    public void shopperRecordsInitialSaveState() {
        initialSaveState = SaveButtonState.forFirstProduct().answeredBy(OnlineShopper.named("Shopper"));
    }

    @Given("the shopper has saved the first product")
    public void shopperHasSavedFirstProduct() {
        var actor = OnlineShopper.named("Shopper");
        var currentState = SaveButtonState.forFirstProduct().answeredBy(actor);
        if (!"true".equals(currentState)) {
            actor.attemptsTo(ToggleSaveProduct.forFirstProduct());
            var driver = BrowseTheWeb.as(actor).getDriver();
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> "true".equals(
                    d.findElements(By.cssSelector("[data-testid='save-button']"))
                        .get(0).getAttribute("aria-pressed")));
        }
    }

    @Given("the shopper is on the saved page")
    public void shopperIsOnSavedPage() {
        OnlineShopper.named("Shopper").attemptsTo(NavigateTo.theSavedPage());
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/saved"));
    }

    @When("the shopper toggles the save button for the first product")
    public void shopperTogglesSaveButtonForFirstProduct() {
        OnlineShopper.named("Shopper").attemptsTo(ToggleSaveProduct.forFirstProduct());
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    @When("the shopper toggles the save button again")
    public void shopperTogglesSaveButtonAgain() {
        OnlineShopper.named("Shopper").attemptsTo(ToggleSaveProduct.forFirstProduct());
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    @When("the shopper navigates to the saved page")
    public void shopperNavigatesToSavedPage() {
        OnlineShopper.named("Shopper").attemptsTo(NavigateTo.theSavedPage());
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/saved"));
    }

    @When("the shopper clicks the wishlist link")
    public void shopperClicksWishlistLink() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        driver.findElement(By.cssSelector("[data-testid='wishlist-link']")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/saved"));
    }

    @Then("the save state of the first product should have changed")
    public void saveStateShouldHaveChanged() {
        String newState = SaveButtonState.forFirstProduct().answeredBy(OnlineShopper.named("Shopper"));
        assertThat(newState).isNotEqualTo(initialSaveState);
    }

    @Then("the save state should be restored to the initial state")
    public void saveStateShouldBeRestoredToInitialState() {
        String finalState = SaveButtonState.forFirstProduct().answeredBy(OnlineShopper.named("Shopper"));
        assertThat(finalState).isEqualTo(initialSaveState);
    }

    @Then("the saved count should be visible and show a number")
    public void savedCountShouldBeVisibleAndShowNumber() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='saved-count']")));
        String text = driver.findElement(By.cssSelector("[data-testid='saved-count']")).getText();
        String number = text.replaceAll("[^0-9]", "").trim();
        assertThat(number).matches("\\d+");
    }

    @Then("the wishlist link should be visible")
    public void wishlistLinkShouldBeVisible() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        assertThat(driver.findElements(By.cssSelector("[data-testid='wishlist-link']")))
            .isNotEmpty();
    }

    @Then("the URL should contain \\/saved")
    public void urlShouldContainSaved() {
        String url = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver().getCurrentUrl();
        assertThat(url).contains("/saved");
    }

    @Then("the save button should be visible and functional on the detail page")
    public void saveButtonShouldBeVisibleAndFunctional() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var saveButtons = driver.findElements(By.cssSelector("[data-testid='save-button']"));
        assertThat(saveButtons).isNotEmpty();
        assertThat(saveButtons.get(0).isDisplayed()).isTrue();
        assertThat(saveButtons.get(0).isEnabled()).isTrue();
    }
}
