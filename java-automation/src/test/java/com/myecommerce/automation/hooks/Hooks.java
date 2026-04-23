package com.myecommerce.automation.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.myecommerce.automation.driver.web.PlaywrightPageHolder;
import com.myecommerce.automation.dsl.protocols.Channel;
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

    private static final boolean WEB_CHANNEL = Channel.current() == Channel.WEB;

    private WebDriver driver;
    private Playwright playwright;
    private Browser playwrightBrowser;
    private Page playwrightPage;

    @Before(order = 1)
    public void setUpStage() {
        if (!WEB_CHANNEL) return;
        boolean headed = Boolean.parseBoolean(System.getProperty("headed", "false"));
        if (isPlaywright()) {
            playwright = Playwright.create();
            BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions().setHeadless(!headed);
            playwrightBrowser = playwright.chromium().launch(opts);
            playwrightPage = playwrightBrowser.newPage();
            PlaywrightPageHolder.set(playwrightPage);
        } else {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
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
    }

    @Before(order = 2)
    public void clearCartState() {
        if (!WEB_CHANNEL) return;
        if (isPlaywright()) {
            playwrightPage.navigate("http://localhost:3001");
            playwrightPage.evaluate("() => { try { " +
                "localStorage.removeItem('cart');" +
                "localStorage.removeItem('cartItems');" +
                "localStorage.removeItem('selectedDelivery');" +
                "} catch(e) {} }");
        } else {
            driver.get("http://localhost:3001");
            ((JavascriptExecutor) driver).executeScript(
                "try {" +
                "  localStorage.removeItem('cart');" +
                "  localStorage.removeItem('cartItems');" +
                "  localStorage.removeItem('selectedDelivery');" +
                "} catch(e) {}"
            );
        }
    }

    @After
    public void tearDownStage() {
        if (!WEB_CHANNEL) return;
        if (isPlaywright()) {
            PlaywrightPageHolder.clear();
            if (playwrightPage != null) { playwrightPage.close(); playwrightPage = null; }
            if (playwrightBrowser != null) { playwrightBrowser.close(); playwrightBrowser = null; }
            if (playwright != null) { playwright.close(); playwright = null; }
        } else {
            OnStage.drawTheCurtain();
            if (driver != null) { driver.quit(); driver = null; }
        }
    }

    private static boolean isPlaywright() {
        return "playwright".equals(System.getProperty("browser.impl", "selenium"));
    }
}
