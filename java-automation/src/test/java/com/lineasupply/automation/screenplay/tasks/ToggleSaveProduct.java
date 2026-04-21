package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;

public class ToggleSaveProduct implements Task {

    private final int index;

    private ToggleSaveProduct(int index) {
        this.index = index;
    }

    public static ToggleSaveProduct forFirstProduct() {
        return new ToggleSaveProduct(0);
    }

    public static ToggleSaveProduct forProductAtIndex(int index) {
        return new ToggleSaveProduct(index);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var buttons = BrowseTheWeb.as(actor).getDriver()
            .findElements(By.cssSelector("[data-testid='save-button']"));
        buttons.get(index).click();
    }
}
