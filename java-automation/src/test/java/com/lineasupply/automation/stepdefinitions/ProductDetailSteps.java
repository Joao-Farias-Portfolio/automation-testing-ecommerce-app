package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.questions.CartItemCount;
import com.lineasupply.automation.screenplay.tasks.AddProductToCart;
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

public class ProductDetailSteps {

    private String capturedProductTitle;
    private int initialCartCount;

    @Given("the shopper is viewing the first product")
    public void shopperIsViewingFirstProduct() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));

        capturedProductTitle = driver
            .findElements(By.cssSelector("[data-testid='product-title']"))
            .get(0).getText();

        driver.findElements(By.cssSelector("[data-testid='product-card']")).get(0).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlMatches(".*/products/\\d+"));
    }

    @Given("the shopper notes the cart count on the detail page")
    public void shopperNotesCartCountOnDetailPage() {
        initialCartCount = CartItemCount.displayed().answeredBy(OnlineShopper.named("Shopper"));
    }

    @When("the shopper clicks the first product card")
    public void shopperClicksFirstProductCard() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        driver.findElements(By.cssSelector("[data-testid='product-card']")).get(0).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlMatches(".*/products/\\d+"));
    }

    @When("the shopper adds the product to the cart from the detail page")
    public void shopperAddsProductFromDetailPage() {
        OnlineShopper.named("Shopper").attemptsTo(AddProductToCart.forFirstProduct());
    }

    @When("the shopper navigates back")
    public void shopperNavigatesBack() {
        BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver().navigate().back();
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @Then("the URL should match the product detail pattern")
    public void urlShouldMatchProductDetailPattern() {
        String url = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver().getCurrentUrl();
        assertThat(url).matches(".*/products/\\d+");
    }

    @Then("the product title should be visible on the detail page")
    public void productTitleShouldBeVisibleOnDetailPage() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='product-title']")));
        var title = driver.findElement(By.cssSelector("[data-testid='product-title']")).getText();
        assertThat(title).isNotBlank();
    }

    @Then("the product detail page should show price, description and image")
    public void detailPageShouldShowPriceDescriptionAndImage() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        assertThat(driver.findElement(By.cssSelector("[data-testid='product-price']"))
            .getText()).matches(".*\\$\\d+.*");
        assertThat(driver.findElement(By.cssSelector("[data-testid='product-description']"))
            .getText()).isNotBlank();
        assertThat(driver.findElements(By.cssSelector("[data-testid='product-detail-image']")))
            .isNotEmpty();
    }

    @Then("the product title should match the one from the listing")
    public void productTitleShouldMatchTheListing() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        String detailTitle = driver.findElement(
            By.cssSelector("[data-testid='product-title']")).getText();
        assertThat(detailTitle).contains(capturedProductTitle.trim());
    }

    @Then("the cart count should have increased")
    public void cartCountShouldHaveIncreased() {
        int current = CartItemCount.displayed().answeredBy(OnlineShopper.named("Shopper"));
        assertThat(current).isGreaterThan(initialCartCount);
    }

    @Then("the add to cart button should show Added to Cart and be disabled")
    public void addToCartButtonShouldShowAddedToCartAndBeDisabled() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        var button = driver.findElement(By.cssSelector("[data-testid='add-to-cart']"));
        assertThat(button.getText()).containsIgnoringCase("Added to Cart");
        assertThat(button.isEnabled()).isFalse();
    }

    @Then("the shopper should be back on the product listing")
    public void shopperShouldBeBackOnProductListing() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        assertThat(driver.findElements(By.cssSelector("[data-testid='product-card']")))
            .isNotEmpty();
        assertThat(driver.getCurrentUrl()).matches(".*/(\\?.*)?$");
    }
}
