package com.myecommerce.automation.driver.web;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.myecommerce.automation.driver.ports.BrowserPort;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class PlaywrightBrowserPort implements BrowserPort {

    private final Page page;

    public PlaywrightBrowserPort(Page page) {
        this.page = page;
    }

    public static PlaywrightBrowserPort fromCurrentPage() {
        return new PlaywrightBrowserPort(PlaywrightPageHolder.get());
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Override
    public void navigateTo(String url) {
        page.navigate(url);
    }

    @Override
    public void navigateBack() {
        page.goBack();
    }

    @Override
    public String currentUrl() {
        return page.url();
    }

    // ── Element state ─────────────────────────────────────────────────────────

    @Override
    public boolean isVisible(String css) {
        return page.isVisible(css);
    }

    @Override
    public boolean isPresent(String css) {
        return page.locator(css).count() > 0;
    }

    @Override
    public boolean isEnabled(String css) {
        return page.isEnabled(css);
    }

    @Override
    public boolean isSelected(String css) {
        return page.isChecked(css);
    }

    @Override
    public int count(String css) {
        return page.locator(css).count();
    }

    // ── Element content ───────────────────────────────────────────────────────

    @Override
    public String text(String css) {
        Locator loc = page.locator(css);
        return loc.count() > 0 ? loc.first().innerText().trim() : "";
    }

    @Override
    public String attribute(String css, String attr) {
        Locator loc = page.locator(css);
        if (loc.count() == 0) return "";
        String val = loc.first().getAttribute(attr);
        return val != null ? val : "";
    }

    // ── Nth-element queries ───────────────────────────────────────────────────

    @Override
    public boolean isNthEnabled(String css, int index) {
        Locator loc = page.locator(css);
        return index < loc.count() && loc.nth(index).isEnabled();
    }

    @Override
    public boolean isNthSelected(String css, int index) {
        Locator loc = page.locator(css);
        return index < loc.count() && loc.nth(index).isChecked();
    }

    @Override
    public String nthAttribute(String css, int index, String attr) {
        Locator loc = page.locator(css);
        if (index >= loc.count()) return "";
        String val = loc.nth(index).getAttribute(attr);
        return val != null ? val : "";
    }

    @Override
    public String nthText(String css, int index) {
        Locator loc = page.locator(css);
        return index < loc.count() ? loc.nth(index).innerText().trim() : "";
    }

    // ── Scoped queries ────────────────────────────────────────────────────────

    @Override
    public boolean isSelectedWithin(String parentCss, int parentIndex, String childCss) {
        Locator parent = page.locator(parentCss);
        if (parentIndex >= parent.count()) return false;
        Locator child = parent.nth(parentIndex).locator(childCss);
        return child.count() > 0 && child.first().isChecked();
    }

    @Override
    public String attributeWithin(String parentCss, int parentIndex, String childCss, String attr) {
        Locator parent = page.locator(parentCss);
        if (parentIndex >= parent.count()) return "";
        Locator child = parent.nth(parentIndex).locator(childCss);
        if (child.count() == 0) return "";
        String val = child.first().getAttribute(attr);
        return val != null ? val : "";
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Override
    public void click(String css) {
        page.click(css);
    }

    @Override
    public void clickNth(String css, int index) {
        page.locator(css).nth(index).click();
    }

    @Override
    public void clickXpath(String xpath) {
        page.locator("xpath=" + xpath).click();
    }

    @Override
    public void sendKeys(String css, String text, boolean submitAfter) {
        page.fill(css, text);
        if (submitAfter) page.press(css, "Enter");
    }

    @Override
    public void setReactInputValue(String css, String value) {
        page.evaluate("""
            (args) => {
                const el = document.querySelector(args[0]);
                const setter = Object.getOwnPropertyDescriptor(
                    window.HTMLInputElement.prototype, 'value').set;
                setter.call(el, args[1]);
                el.dispatchEvent(new Event('input', { bubbles: true }));
                el.dispatchEvent(new Event('change', { bubbles: true }));
            }
            """, new Object[]{css, value});
    }

    // ── JavaScript extraction ─────────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> extractAllViaScript(String script) {
        return (List<Map<String, String>>) page.evaluate(script);
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return page.evaluate(script, args.length == 1 ? args[0] : args);
    }

    // ── Waits ─────────────────────────────────────────────────────────────────

    @Override
    public void waitUntilVisible(String css) {
        page.waitForSelector(css, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10_000));
    }

    @Override
    public void waitUntilPresent(String css) {
        page.waitForSelector(css, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.ATTACHED)
            .setTimeout(10_000));
    }

    @Override
    public void waitUntilCountMoreThan(String css, int count) {
        page.waitForCondition(
            () -> page.locator(css).count() > count,
            new Page.WaitForConditionOptions().setTimeout(10_000));
    }

    @Override
    public void waitUntilUrlContains(String fragment) {
        page.waitForURL("**" + fragment + "**",
            new Page.WaitForURLOptions().setTimeout(10_000));
    }

    @Override
    public void waitUntilUrlMatches(String regex) {
        page.waitForURL(Pattern.compile(regex),
            new Page.WaitForURLOptions().setTimeout(10_000));
    }

    @Override
    public void waitUntilAttributeChanges(String css, int index, String attr, String previousValue) {
        page.waitForCondition(() -> {
            Locator loc = page.locator(css);
            if (index >= loc.count()) return false;
            String current = loc.nth(index).getAttribute(attr);
            return !previousValue.equals(current);
        }, new Page.WaitForConditionOptions().setTimeout(5_000));
    }

    @Override
    public void waitUntilAnyPresent(String... cssList) {
        page.waitForCondition(
            () -> Arrays.stream(cssList).anyMatch(css -> page.locator(css).count() > 0),
            new Page.WaitForConditionOptions().setTimeout(10_000));
    }

    @Override
    public void waitUntilCondition(Supplier<Boolean> condition, int timeoutSeconds) {
        page.waitForCondition(
            condition::get,
            new Page.WaitForConditionOptions().setTimeout(timeoutSeconds * 1000.0));
    }

    // ── Extra (mirrors SeleniumBrowserPort) ───────────────────────────────────

    public boolean tryWaitUntilPresent(String css, int timeoutSeconds) {
        try {
            page.waitForSelector(css, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(timeoutSeconds * 1000.0));
            return true;
        } catch (PlaywrightException ignored) {
            return false;
        }
    }
}
