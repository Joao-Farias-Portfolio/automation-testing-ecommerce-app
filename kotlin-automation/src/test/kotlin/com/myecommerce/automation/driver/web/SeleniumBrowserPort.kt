package com.myecommerce.automation.driver.web

import com.myecommerce.automation.driver.ports.BrowserPort
import net.serenitybdd.screenplay.abilities.BrowseTheWeb
import net.serenitybdd.screenplay.actions.Open
import net.serenitybdd.screenplay.actors.OnStage
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class SeleniumBrowserPort : BrowserPort {

    private fun driver(): WebDriver = BrowseTheWeb.`as`(OnStage.theActorCalled("Shopper")).driver

    override fun navigateTo(url: String) {
        OnStage.theActorCalled("Shopper").attemptsTo(Open.url(url))
    }

    override fun navigateBack() = driver().navigate().back()

    override fun currentUrl(): String = driver().currentUrl ?: ""

    override fun isVisible(css: String): Boolean {
        val els = driver().findElements(By.cssSelector(css))
        return els.isNotEmpty() && safeIsDisplayed { els.first().isDisplayed }
    }

    override fun isPresent(css: String): Boolean = driver().findElements(By.cssSelector(css)).isNotEmpty()

    override fun isEnabled(css: String): Boolean {
        val els = driver().findElements(By.cssSelector(css))
        return els.isNotEmpty() && els.first().isEnabled
    }

    override fun isSelected(css: String): Boolean {
        val els = driver().findElements(By.cssSelector(css))
        return els.isNotEmpty() && els.first().isSelected
    }

    override fun count(css: String): Int = driver().findElements(By.cssSelector(css)).size

    override fun text(css: String): String {
        val els = driver().findElements(By.cssSelector(css))
        return if (els.isEmpty()) "" else els.first().text.trim()
    }

    override fun attribute(css: String, attr: String): String {
        val els = driver().findElements(By.cssSelector(css))
        return if (els.isEmpty()) "" else els.first().getAttribute(attr) ?: ""
    }

    override fun isNthEnabled(css: String, index: Int): Boolean {
        val els = driver().findElements(By.cssSelector(css))
        return index < els.size && els[index].isEnabled
    }

    override fun isNthSelected(css: String, index: Int): Boolean {
        val els = driver().findElements(By.cssSelector(css))
        return index < els.size && els[index].isSelected
    }

    override fun nthAttribute(css: String, index: Int, attr: String): String {
        val els = driver().findElements(By.cssSelector(css))
        return if (index >= els.size) "" else els[index].getAttribute(attr) ?: ""
    }

    override fun nthText(css: String, index: Int): String {
        val els = driver().findElements(By.cssSelector(css))
        return if (index < els.size) els[index].text.trim() else ""
    }

    override fun isSelectedWithin(parentCss: String, parentIndex: Int, childCss: String): Boolean {
        val parents = driver().findElements(By.cssSelector(parentCss))
        if (parentIndex >= parents.size) return false
        val children = parents[parentIndex].findElements(By.cssSelector(childCss))
        return children.isNotEmpty() && children.first().isSelected
    }

    override fun attributeWithin(parentCss: String, parentIndex: Int, childCss: String, attr: String): String {
        val parents = driver().findElements(By.cssSelector(parentCss))
        if (parentIndex >= parents.size) return ""
        val children = parents[parentIndex].findElements(By.cssSelector(childCss))
        return if (children.isEmpty()) "" else children.first().getAttribute(attr) ?: ""
    }

    override fun click(css: String) = driver().findElement(By.cssSelector(css)).click()

    override fun clickNth(css: String, index: Int) = driver().findElements(By.cssSelector(css))[index].click()

    override fun clickXpath(xpath: String) = driver().findElement(By.xpath(xpath)).click()

    override fun sendKeys(css: String, text: String, submitAfter: Boolean) {
        val el = driver().findElement(By.cssSelector(css))
        el.clear()
        el.sendKeys(text)
        if (submitAfter) el.sendKeys(Keys.ENTER)
    }

    override fun setReactInputValue(css: String, value: String) {
        val input = driver().findElement(By.cssSelector(css))
        val js = driver() as JavascriptExecutor
        js.executeScript("""
            var setter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype, 'value').set;
            setter.call(arguments[0], arguments[1]);
            arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
            arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
        """, input, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun extractAllViaScript(script: String): List<Map<String, String>> =
        (driver() as JavascriptExecutor).executeScript(script) as List<Map<String, String>>

    override fun executeScript(script: String, vararg args: Any?): Any? =
        (driver() as JavascriptExecutor).executeScript(script, *args)

    override fun waitUntilVisible(css: String) {
        wait(10).until { ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)).apply(it) }
    }

    override fun waitUntilPresent(css: String) {
        wait(10).until { ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)).apply(it) != null }
    }

    override fun waitUntilCountMoreThan(css: String, count: Int) {
        wait(10).until { ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(css), count).apply(it) != null }
    }

    override fun waitUntilUrlContains(fragment: String) {
        wait(10).until { ExpectedConditions.urlContains(fragment).apply(it) == true }
    }

    override fun waitUntilUrlMatches(regex: String) {
        wait(10).until { ExpectedConditions.urlMatches(regex).apply(it) == true }
    }

    override fun waitUntilAttributeChanges(css: String, index: Int, attr: String, previousValue: String) {
        wait(5).until {
            val els = driver().findElements(By.cssSelector(css))
            index < els.size && els[index].getAttribute(attr) != previousValue
        }
    }

    override fun waitUntilAnyPresent(vararg cssList: String) {
        val conditions = cssList.map { ExpectedConditions.presenceOfElementLocated(By.cssSelector(it)) }
        wait(10).until { ExpectedConditions.or(*conditions.toTypedArray()).apply(it) == true }
    }

    override fun waitUntilCondition(condition: () -> Boolean, timeoutSeconds: Int) {
        wait(timeoutSeconds).until { condition() }
    }

    override fun tryWaitUntilPresent(css: String, timeoutSeconds: Int): Boolean =
        try {
            wait(timeoutSeconds).until { driver().findElements(By.cssSelector(css)).isNotEmpty() }
            true
        } catch (_: TimeoutException) {
            false
        }

    private fun wait(seconds: Int) = WebDriverWait(driver(), Duration.ofSeconds(seconds.toLong()))

    private fun safeIsDisplayed(check: () -> Boolean): Boolean =
        try { check() } catch (_: Exception) { false }
}
