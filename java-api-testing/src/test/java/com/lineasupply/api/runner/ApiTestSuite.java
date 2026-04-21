package com.lineasupply.api.runner;

import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;
import io.cucumber.junit.CucumberOptions;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.lineasupply.api",
    tags = "not @wip",
    plugin = {"pretty"}
)
public class ApiTestSuite {
}
