package com.myecommerce.automation.driver.ports;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface BrowserPort {

    // Navigation
    void navigateTo(String url);
    void navigateBack();
    String currentUrl();

    // Element state
    boolean isVisible(String css);
    boolean isPresent(String css);
    boolean isEnabled(String css);
    boolean isSelected(String css);
    int count(String css);

    // Element content
    String text(String css);
    String attribute(String css, String attr);

    // Nth-element queries (flat list, zero-based index)
    boolean isNthEnabled(String css, int index);
    boolean isNthSelected(String css, int index);
    String nthAttribute(String css, int index, String attr);
    String nthText(String css, int index);

    // Scoped queries — within the nth element matching parentCss
    boolean isSelectedWithin(String parentCss, int parentIndex, String childCss);
    String attributeWithin(String parentCss, int parentIndex, String childCss, String attr);

    // Actions
    void click(String css);
    void clickNth(String css, int index);
    void clickXpath(String xpath);
    void sendKeys(String css, String text, boolean submitAfter);
    void setReactInputValue(String css, String value);

    // JavaScript extraction
    List<Map<String, String>> extractAllViaScript(String script);
    Object executeScript(String script, Object... args);

    // Waits
    void waitUntilVisible(String css);
    void waitUntilPresent(String css);
    void waitUntilCountMoreThan(String css, int count);
    void waitUntilUrlContains(String fragment);
    void waitUntilUrlMatches(String regex);
    void waitUntilAttributeChanges(String css, int index, String attr, String previousValue);
    void waitUntilAnyPresent(String... cssList);
    void waitUntilCondition(Supplier<Boolean> condition, int timeoutSeconds);
}
