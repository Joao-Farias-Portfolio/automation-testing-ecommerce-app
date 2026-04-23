package com.lineasupply.automation.dsl.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Hooks {

    private WebDriver driver;

    @Before(order = 1)
    public void setUpStage() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        boolean headed = Boolean.parseBoolean(System.getProperty("headed", "false"));
        if (!headed) {
            options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080",
                "--disable-extensions"
            );
        } else {
            options.addArguments("--window-size=1920,1080", "--disable-extensions");
        }
        driver = new ChromeDriver(options);
        OnStage.setTheStage(Cast.whereEveryoneCan(BrowseTheWeb.with(driver)));
    }

    @Before(order = 2)
    public void clearCartState() {
        driver.get("http://localhost:3001");
        ((JavascriptExecutor) driver).executeScript(
            "try {" +
            "  localStorage.removeItem('cart');" +
            "  localStorage.removeItem('cartItems');" +
            "  localStorage.removeItem('selectedDelivery');" +
            "} catch(e) {}"
        );
    }

    @After
    public void tearDownStage() {
        OnStage.drawTheCurtain();
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
