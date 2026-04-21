package com.lineasupply.automation.screenplay.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class CartItemCount implements Question<Integer> {

    public static CartItemCount displayed() {
        return new CartItemCount();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        var badges = driver.findElements(By.cssSelector("[data-testid='cart-count']"));
        if (badges.isEmpty()) return 0;
        String text = badges.get(0).getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}
