package com.myecommerce.automation.driver.ports

interface BrowserPort {

    // Navigation
    fun navigateTo(url: String)
    fun navigateBack()
    fun currentUrl(): String

    // Element state
    fun isVisible(css: String): Boolean
    fun isPresent(css: String): Boolean
    fun isEnabled(css: String): Boolean
    fun isSelected(css: String): Boolean
    fun count(css: String): Int

    // Element content
    fun text(css: String): String
    fun attribute(css: String, attr: String): String

    // Nth-element queries (flat list, zero-based index)
    fun isNthEnabled(css: String, index: Int): Boolean
    fun isNthSelected(css: String, index: Int): Boolean
    fun nthAttribute(css: String, index: Int, attr: String): String
    fun nthText(css: String, index: Int): String

    // Scoped queries — within the nth element matching parentCss
    fun isSelectedWithin(parentCss: String, parentIndex: Int, childCss: String): Boolean
    fun attributeWithin(parentCss: String, parentIndex: Int, childCss: String, attr: String): String

    // Actions
    fun click(css: String)
    fun clickNth(css: String, index: Int)
    fun clickXpath(xpath: String)
    fun sendKeys(css: String, text: String, submitAfter: Boolean)
    fun setReactInputValue(css: String, value: String)

    // JavaScript extraction
    fun extractAllViaScript(script: String): List<Map<String, String>>
    fun executeScript(script: String, vararg args: Any?): Any?

    // Waits
    fun waitUntilVisible(css: String)
    fun waitUntilPresent(css: String)
    fun waitUntilCountMoreThan(css: String, count: Int)
    fun waitUntilUrlContains(fragment: String)
    fun waitUntilUrlMatches(regex: String)
    fun waitUntilAttributeChanges(css: String, index: Int, attr: String, previousValue: String)
    fun waitUntilAnyPresent(vararg cssList: String)
    fun waitUntilCondition(condition: () -> Boolean, timeoutSeconds: Int)
    fun tryWaitUntilPresent(css: String, timeoutSeconds: Int): Boolean
}
