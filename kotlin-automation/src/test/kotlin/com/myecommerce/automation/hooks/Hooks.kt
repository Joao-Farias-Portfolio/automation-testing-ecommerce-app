package com.myecommerce.automation.hooks

import com.myecommerce.automation.dsl.protocols.Channel
import io.cucumber.java.After
import io.cucumber.java.Before
import io.github.bonigarcia.wdm.WebDriverManager
import net.serenitybdd.screenplay.abilities.BrowseTheWeb
import net.serenitybdd.screenplay.actors.Cast
import net.serenitybdd.screenplay.actors.OnStage
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

class Hooks {

    private val webChannel = Channel.current() == Channel.WEB
    private val headed = System.getProperty("headed", "false").toBoolean()

    private var driver: WebDriver? = null

    @Before(order = 1)
    fun setUpStage() {
        if (!webChannel) return
        WebDriverManager.chromedriver().setup()
        val options = ChromeOptions().apply {
            if (!headed) {
                addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080", "--disable-extensions")
            } else {
                addArguments("--window-size=1920,1080", "--disable-extensions")
            }
        }
        driver = ChromeDriver(options).also { d ->
            OnStage.setTheStage(Cast.whereEveryoneCan(BrowseTheWeb.with(d)))
        }
    }

    @Before(order = 2)
    fun clearCartState() {
        if (!webChannel) return
        driver?.let { d ->
            d.get("http://localhost:3001")
            (d as JavascriptExecutor).executeScript("""
                try {
                    localStorage.removeItem('cart');
                    localStorage.removeItem('cartItems');
                    localStorage.removeItem('selectedDelivery');
                } catch(e) {}
            """)
        }
    }

    @After
    fun tearDownStage() {
        if (!webChannel) return
        OnStage.drawTheCurtain()
        driver?.quit()
        driver = null
    }
}
