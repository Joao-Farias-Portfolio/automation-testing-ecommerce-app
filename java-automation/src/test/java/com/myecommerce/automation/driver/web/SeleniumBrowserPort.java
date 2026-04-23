package com.myecommerce.automation.driver.web;

import com.myecommerce.automation.driver.ports.BrowserPort;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.actors.OnStage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SeleniumBrowserPort implements BrowserPort {

    private WebDriver driver() {
        return BrowseTheWeb.as(OnStage.theActorCalled("Shopper")).getDriver();
    }

    @Override
    public void navigateTo(String url) {
        OnStage.theActorCalled("Shopper").attemptsTo(Open.url(url));
    }

    @Override
    public void navigateBack() {
        driver().navigate().back();
    }

    @Override
    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    @Override
    public boolean isVisible(String css) {
        var els = driver().findElements(By.cssSelector(css));
        return !els.isEmpty() && safeIsDisplayed(els.getFirst());
    }

    @Override
    public boolean isPresent(String css) {
        return !driver().findElements(By.cssSelector(css)).isEmpty();
    }

    @Override
    public boolean isEnabled(String css) {
        var els = driver().findElements(By.cssSelector(css));
        return !els.isEmpty() && els.getFirst().isEnabled();
    }

    @Override
    public boolean isSelected(String css) {
        var els = driver().findElements(By.cssSelector(css));
        return !els.isEmpty() && els.getFirst().isSelected();
    }

    @Override
    public int count(String css) {
        return driver().findElements(By.cssSelector(css)).size();
    }

    @Override
    public String text(String css) {
        var els = driver().findElements(By.cssSelector(css));
        return els.isEmpty() ? "" : els.getFirst().getText().trim();
    }

    @Override
    public String attribute(String css, String attr) {
        var els = driver().findElements(By.cssSelector(css));
        if (els.isEmpty()) return "";
        String val = els.getFirst().getAttribute(attr);
        return val != null ? val : "";
    }

    @Override
    public boolean isNthEnabled(String css, int index) {
        var els = driver().findElements(By.cssSelector(css));
        return index < els.size() && els.get(index).isEnabled();
    }

    @Override
    public boolean isNthSelected(String css, int index) {
        var els = driver().findElements(By.cssSelector(css));
        return index < els.size() && els.get(index).isSelected();
    }

    @Override
    public String nthAttribute(String css, int index, String attr) {
        var els = driver().findElements(By.cssSelector(css));
        if (index >= els.size()) return "";
        String val = els.get(index).getAttribute(attr);
        return val != null ? val : "";
    }

    @Override
    public String nthText(String css, int index) {
        var els = driver().findElements(By.cssSelector(css));
        return index < els.size() ? els.get(index).getText().trim() : "";
    }

    @Override
    public boolean isSelectedWithin(String parentCss, int parentIndex, String childCss) {
        var parents = driver().findElements(By.cssSelector(parentCss));
        if (parentIndex >= parents.size()) return false;
        var children = parents.get(parentIndex).findElements(By.cssSelector(childCss));
        return !children.isEmpty() && children.getFirst().isSelected();
    }

    @Override
    public String attributeWithin(String parentCss, int parentIndex, String childCss, String attr) {
        var parents = driver().findElements(By.cssSelector(parentCss));
        if (parentIndex >= parents.size()) return "";
        var children = parents.get(parentIndex).findElements(By.cssSelector(childCss));
        if (children.isEmpty()) return "";
        String val = children.getFirst().getAttribute(attr);
        return val != null ? val : "";
    }

    @Override
    public void click(String css) {
        driver().findElement(By.cssSelector(css)).click();
    }

    @Override
    public void clickNth(String css, int index) {
        driver().findElements(By.cssSelector(css)).get(index).click();
    }

    @Override
    public void clickXpath(String xpath) {
        driver().findElement(By.xpath(xpath)).click();
    }

    @Override
    public void sendKeys(String css, String text, boolean submitAfter) {
        var el = driver().findElement(By.cssSelector(css));
        el.clear();
        el.sendKeys(text);
        if (submitAfter) el.sendKeys(Keys.ENTER);
    }

    @Override
    public void setReactInputValue(String css, String value) {
        var input = driver().findElement(By.cssSelector(css));
        var js = (JavascriptExecutor) driver();
        js.executeScript("""
            var setter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype, 'value').set;
            setter.call(arguments[0], arguments[1]);
            arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
            arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
            """, input, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> extractAllViaScript(String script) {
        return (List<Map<String, String>>) ((JavascriptExecutor) driver()).executeScript(script);
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver()).executeScript(script, args);
    }

    @Override
    public void waitUntilVisible(String css) {
        wait(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    @Override
    public void waitUntilPresent(String css) {
        wait(10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
    }

    @Override
    public void waitUntilCountMoreThan(String css, int count) {
        wait(10).until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(css), count));
    }

    @Override
    public void waitUntilUrlContains(String fragment) {
        wait(10).until(ExpectedConditions.urlContains(fragment));
    }

    @Override
    public void waitUntilUrlMatches(String regex) {
        wait(10).until(ExpectedConditions.urlMatches(regex));
    }

    @Override
    public void waitUntilAttributeChanges(String css, int index, String attr, String previousValue) {
        wait(5).until(ignored -> {
            var els = driver().findElements(By.cssSelector(css));
            if (index >= els.size()) return false;
            return !previousValue.equals(els.get(index).getAttribute(attr));
        });
    }

    @Override
    public void waitUntilAnyPresent(String... cssList) {
        var conditions = Arrays.stream(cssList)
            .map(css -> ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)))
            .toList();
        wait(10).until(ExpectedConditions.or(conditions.toArray(new org.openqa.selenium.support.ui.ExpectedCondition[0])));
    }

    @Override
    public void waitUntilCondition(Supplier<Boolean> condition, int timeoutSeconds) {
        wait(timeoutSeconds).until(ignored -> condition.get());
    }

    public boolean tryWaitUntilPresent(String css, int timeoutSeconds) {
        try {
            wait(timeoutSeconds).until(ignored -> !driver().findElements(By.cssSelector(css)).isEmpty());
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private WebDriverWait wait(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    private boolean safeIsDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }
}
