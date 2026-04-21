package com.lineasupply.automation.screenplay.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;

public class CartTotal implements Question<String> {

    public static CartTotal displayed() {
        return new CartTotal();
    }

    @Override
    public String answeredBy(Actor actor) {
        var totals = BrowseTheWeb.as(actor).getDriver()
            .findElements(By.cssSelector("[data-testid='cart-total']"));
        return totals.isEmpty() ? "" : totals.get(0).getText().trim();
    }
}
