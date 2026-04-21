package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AddProductToCart implements Task {

    private final int index;

    private AddProductToCart(int index) {
        this.index = index;
    }

    public static AddProductToCart forFirstProduct() {
        return new AddProductToCart(0);
    }

    public static AddProductToCart forProductAtIndex(int index) {
        return new AddProductToCart(index);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='add-to-cart']"), 0));
        driver.findElements(By.cssSelector("[data-testid='add-to-cart']")).get(index).click();
    }
}
