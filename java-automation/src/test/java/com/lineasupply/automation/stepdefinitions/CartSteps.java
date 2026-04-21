package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.screenplay.actors.OnlineShopper;
import com.lineasupply.automation.screenplay.questions.CartItemCount;
import com.lineasupply.automation.screenplay.questions.CartTotal;
import com.lineasupply.automation.screenplay.tasks.AddProductToCart;
import com.lineasupply.automation.screenplay.tasks.ChangeQuantity;
import com.lineasupply.automation.screenplay.tasks.NavigateTo;
import com.lineasupply.automation.screenplay.tasks.RemoveItemFromCart;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class CartSteps {

    private int initialCartCount;
    private String initialCartTotal;

    @Given("the shopper is on the homepage with products loaded")
    public void shopperOnHomepageWithProductsLoaded() {
        var shopper = OnlineShopper.named("Shopper");
        shopper.attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(shopper).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @Given("the shopper notes the current cart count")
    public void shopperNotesCurrentCartCount() {
        initialCartCount = CartItemCount.displayed().answeredBy(OnlineShopper.named("Shopper"));
    }

    @When("the shopper adds the first product to the cart")
    public void shopperAddsFirstProductToCart() {
        OnlineShopper.named("Shopper").attemptsTo(AddProductToCart.forFirstProduct());
    }

    @When("the shopper adds the second product to the cart")
    public void shopperAddsSecondProductToCart() {
        OnlineShopper.named("Shopper").attemptsTo(AddProductToCart.forProductAtIndex(1));
    }

    @When("the shopper navigates to the cart page")
    public void shopperNavigatesToCartPage() {
        var shopper = OnlineShopper.named("Shopper");
        var driver = BrowseTheWeb.as(shopper).getDriver();
        driver.findElement(By.cssSelector("[data-testid='cart-link']")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/cart"));
    }

    @When("the shopper returns to the homepage")
    public void shopperReturnsToHomepage() {
        OnlineShopper.named("Shopper").attemptsTo(NavigateTo.theHomePage());
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='product-card']")));
    }

    @When("the shopper changes the quantity to {int}")
    public void shopperChangesQuantityTo(int quantity) {
        OnlineShopper.named("Shopper").attemptsTo(ChangeQuantity.to(quantity));
    }

    @When("the shopper removes the first cart item")
    public void shopperRemovesFirstCartItem() {
        OnlineShopper.named("Shopper").attemptsTo(RemoveItemFromCart.theFirstItem());
    }

    @When("the shopper notes the current cart total")
    public void shopperNotesCurrentCartTotal() {
        initialCartTotal = CartTotal.displayed().answeredBy(OnlineShopper.named("Shopper"));
    }

    @Then("the cart badge should show {int} item(s)")
    public void cartBadgeShouldShow(int expectedCount) {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.textToBe(
                By.cssSelector("[data-testid='cart-count']"), String.valueOf(expectedCount)));
        assertThat(CartItemCount.displayed().answeredBy(OnlineShopper.named("Shopper")))
            .isEqualTo(expectedCount);
    }

    @Then("the cart badge should have increased by {int}")
    public void cartBadgeShouldHaveIncreasedBy(int increment) {
        int expected = initialCartCount + increment;
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.textToBe(
                By.cssSelector("[data-testid='cart-count']"), String.valueOf(expected)));
        assertThat(CartItemCount.displayed().answeredBy(OnlineShopper.named("Shopper")))
            .isEqualTo(expected);
    }

    @Then("the cart should contain at least {int} items")
    public void cartShouldContainAtLeastItems(int minimum) {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='cart-item']"), 0));
        var items = driver.findElements(By.cssSelector("[data-testid='cart-item']"));
        assertThat(items.size()).isGreaterThanOrEqualTo(minimum);
    }

    @Then("the cart total should be visible and show a price")
    public void cartTotalShouldBeVisibleAndShowPrice() {
        String total = CartTotal.displayed().answeredBy(OnlineShopper.named("Shopper"));
        assertThat(total).matches(".*\\$\\d+.*");
    }

    @Then("the cart total should have changed")
    public void cartTotalShouldHaveChanged() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        String capturedInitial = initialCartTotal;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> {
                    var totals = d.findElements(By.cssSelector("[data-testid='cart-total']"));
                    if (totals.isEmpty()) return false;
                    return !totals.get(0).getText().trim().equals(capturedInitial);
                });
        } catch (Exception ignored) {}
        String currentTotal = CartTotal.displayed().answeredBy(OnlineShopper.named("Shopper"));
        assertThat(currentTotal).isNotEqualTo(initialCartTotal);
    }

    @Then("the cart should show an empty state")
    public void cartShouldShowEmptyState() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='empty-cart']")));
    }

    @Then("the first cart item should be visible")
    public void firstCartItemShouldBeVisible() {
        var driver = BrowseTheWeb.as(OnlineShopper.named("Shopper")).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='cart-item']")));
        var items = driver.findElements(By.cssSelector("[data-testid='cart-item']"));
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).isDisplayed()).isTrue();
    }
}
