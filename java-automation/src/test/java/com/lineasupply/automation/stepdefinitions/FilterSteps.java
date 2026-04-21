package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.tasks.NavigateTo;
import com.lineasupply.automation.screenplay.tasks.SearchForProduct;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FilterSteps {

    private String capturedSearchTerm;

    @Given("the shopper is on the homepage with products visible")
    public void shopperOnHomepageWithProductsVisible() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @When("the shopper searches for the first product name")
    public void shopperSearchesForFirstProductName() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var titleElements = driver.findElements(By.cssSelector("[data-testid='product-title']"));
        assertThat(titleElements).isNotEmpty();
        String fullTitle = titleElements.get(0).getText();
        capturedSearchTerm = fullTitle.split(" ")[0];
        OnlineShopper.named("Shopper").attemptsTo(SearchForProduct.withTerm(capturedSearchTerm));
    }

    @When("the shopper searches for {string}")
    public void shopperSearchesFor(String term) {
        capturedSearchTerm = term;
        OnlineShopper.named("Shopper").attemptsTo(SearchForProduct.withTerm(term));
    }

    @Then("the URL should contain the search term")
    public void urlShouldContainSearchTerm() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/search/"));
        assertThat(driver.getCurrentUrl()).contains("/search/" + capturedSearchTerm);
    }

    @Then("search results should be displayed")
    public void searchResultsShouldBeDisplayed() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
        var cards = driver.findElements(By.cssSelector("[data-testid='product-card']"));
        assertThat(cards).isNotEmpty();
    }

    @Then("no results or empty state should be shown")
    public void noResultsOrEmptyStateShouldBeShown() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/search/"));

        var noResults = driver.findElements(By.cssSelector("[data-testid='no-results']"));
        var cards = driver.findElements(By.cssSelector("[data-testid='product-card']"));

        assertThat(!noResults.isEmpty() || cards.isEmpty())
            .as("Either no-results element or zero product cards expected")
            .isTrue();
    }
}
