package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RemoveItemFromCart implements Task {

    private final int index;

    private RemoveItemFromCart(int index) {
        this.index = index;
    }

    public static RemoveItemFromCart theFirstItem() {
        return new RemoveItemFromCart(0);
    }

    public static RemoveItemFromCart atIndex(int index) {
        return new RemoveItemFromCart(index);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='remove-item']"), 0));
        driver.findElements(By.cssSelector("[data-testid='remove-item']")).get(index).click();
    }
}
