package com.lineasupply.automation.screenplay.actors;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

public class OnlineShopper {

    public static Actor named(String name) {
        return OnStage.theActorCalled(name);
    }

    public static Actor current() {
        return OnStage.theActorInTheSpotlight();
    }
}
