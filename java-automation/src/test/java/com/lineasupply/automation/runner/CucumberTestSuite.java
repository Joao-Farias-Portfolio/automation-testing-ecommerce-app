package com.lineasupply.automation.runner;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.lineasupply.automation",
    tags = "not @wip",
    plugin = {"pretty"}
)
public class CucumberTestSuite {}
