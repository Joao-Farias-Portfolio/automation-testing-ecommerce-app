package com.lineasupply.automation.screenplay.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;

public class SaveButtonState implements Question<String> {

    private final int index;

    private SaveButtonState(int index) {
        this.index = index;
    }

    public static SaveButtonState forFirstProduct() {
        return new SaveButtonState(0);
    }

    public static SaveButtonState forProductAtIndex(int index) {
        return new SaveButtonState(index);
    }

    @Override
    public String answeredBy(Actor actor) {
        var buttons = BrowseTheWeb.as(actor).getDriver()
            .findElements(By.cssSelector("[data-testid='save-button']"));
        if (buttons.isEmpty() || index >= buttons.size()) return null;
        return buttons.get(index).getAttribute("aria-pressed");
    }
}
