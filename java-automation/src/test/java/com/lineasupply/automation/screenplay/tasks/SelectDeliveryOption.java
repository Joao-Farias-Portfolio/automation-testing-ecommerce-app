package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;

public class SelectDeliveryOption implements Task {

    private static final String DELIVERY_SECTION_SELECTOR =
        "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
        "[data-testid='shipping-section'], [data-testid='shipping-options']";

    private final int index;

    private SelectDeliveryOption(int index) {
        this.index = index;
    }

    public static SelectDeliveryOption atIndex(int index) {
        return new SelectDeliveryOption(index);
    }

    public static SelectDeliveryOption secondOption() {
        return new SelectDeliveryOption(1);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        var section = driver.findElements(By.cssSelector(DELIVERY_SECTION_SELECTOR));
        if (section.isEmpty()) return;

        var radios = section.get(0).findElements(By.cssSelector("input[type='radio']"));
        if (index < radios.size()) {
            var radio = radios.get(index);
            if (!radio.isSelected()) {
                radio.findElement(By.xpath("../..")).click();
            }
        }
    }
}
