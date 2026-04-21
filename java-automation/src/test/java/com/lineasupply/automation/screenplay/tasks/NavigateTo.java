package com.lineasupply.automation.screenplay.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;

public class NavigateTo implements Task {

    private final String url;

    private NavigateTo(String url) {
        this.url = url;
    }

    public static NavigateTo theHomePage() {
        return new NavigateTo("http://localhost:3001");
    }

    public static NavigateTo theCartPage() {
        return new NavigateTo("http://localhost:3001/cart");
    }

    public static NavigateTo theSavedPage() {
        return new NavigateTo("http://localhost:3001/saved");
    }

    public static NavigateTo theSearchResultsFor(String term) {
        return new NavigateTo("http://localhost:3001/search/" + term);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Open.url(url));
    }
}
