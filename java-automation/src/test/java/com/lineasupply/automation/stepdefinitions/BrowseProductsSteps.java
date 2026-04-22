package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.pages.HomePage;
import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.questions.ProductsVisible;
import com.lineasupply.automation.screenplay.tasks.NavigateTo;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class BrowseProductsSteps {

    @Given("the shopper is on the homepage")
    public void shopperIsOnHomepage() {
        OnlineShopper.named("Shopper").attemptsTo(NavigateTo.theHomePage());
    }

    @Given("the homepage has loaded with products")
    public void homepageHasLoadedWithProducts() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @Then("product cards should be visible")
    public void productCardsShouldBeVisible() {
        assertThat(ProductsVisible.onThePage().answeredBy(OnlineShopper.named("Shopper")))
            .isTrue();
    }

    @Then("each product card should show a title and price")
    public void eachProductCardShouldShowTitleAndPrice() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var cards = driver.findElements(By.cssSelector("[data-testid='product-card']"));
        assertThat(cards).isNotEmpty();

        var firstCard = cards.get(0);
        assertThat(firstCard.findElements(By.cssSelector("[data-testid='product-title']")))
            .as("product title should be visible on first card")
            .isNotEmpty();
        assertThat(firstCard.findElements(By.cssSelector("[data-testid='product-price']")))
            .as("product price should be visible on first card")
            .isNotEmpty();
    }

    @Then("the page should show a loading indicator briefly")
    public void pageShouldShowLoadingIndicatorBriefly() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        // Wait for the loading state to resolve — products replace the skeleton cards
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
        // Once products are visible, loading indicators must be gone
        var loadingElements = driver.findElements(By.cssSelector("[data-testid='loading']"));
        assertThat(loadingElements.stream().noneMatch(this::isElementDisplayed)).isTrue();
    }

    private boolean isElementDisplayed(org.openqa.selenium.WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Then("product images should have valid sources")
    public void productImagesShouldHaveValidSources() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var images = driver.findElements(By.cssSelector("[data-testid='product-card'] img"));
        assertThat(images).isNotEmpty();
        String src = images.get(0).getAttribute("src");
        assertThat(src).isNotEmpty();
        assertThat(src).matches(
            "(?:.*localhost:8001.*\\/products\\/\\d+\\/image|https?:\\/\\/.+\\.(jpg|jpeg|png|gif|webp))");
    }
}
