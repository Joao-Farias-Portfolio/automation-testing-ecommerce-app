package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ChangeQuantity implements Task {

    private final int quantity;

    private ChangeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static ChangeQuantity to(int quantity) {
        return new ChangeQuantity(quantity);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='quantity-display']")));
        var input = driver.findElement(By.cssSelector("[data-testid='quantity-display']"));
        var js = (JavascriptExecutor) driver;
        js.executeScript(
            "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "setter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            input, String.valueOf(quantity));
    }
}
