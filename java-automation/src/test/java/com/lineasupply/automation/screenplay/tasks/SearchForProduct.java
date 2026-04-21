package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class SearchForProduct implements Task {

    private final String term;

    private SearchForProduct(String term) {
        this.term = term;
    }

    public static SearchForProduct withTerm(String term) {
        return new SearchForProduct(term);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        var driver = BrowseTheWeb.as(actor).getDriver();
        var searchInput = driver.findElement(By.cssSelector("input[placeholder*='Search Items']"));
        searchInput.clear();
        searchInput.sendKeys(term);
        searchInput.sendKeys(Keys.ENTER);
    }
}
