package com.lineasupply.automation.dsl;

import org.assertj.core.api.SoftAssertions;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class CartDsl {

    private static final Logger log = Logger.getLogger(CartDsl.class.getName());

    private final LineasupplyProtocol protocol;
    private int    notedItemCount;
    private String notedTotal;

    public CartDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void openHomePageAndWaitForProducts() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("home page loaded with products");
    }

    public void noteCurrentCartCount() {
        notedItemCount = protocol.getCartState().itemCount();
        log.fine("noted cart count: " + notedItemCount);
    }

    public void noteCurrentCartTotal() {
        notedTotal = protocol.getCartState().total();
        log.fine("noted cart total: " + notedTotal);
    }

    public void addProduct() {
        protocol.addProductToCart();
        log.fine("added product to cart");
    }

    public void navigateToCart() {
        protocol.openCartPage();
        log.fine("navigated to cart page");
    }

    public void returnToHomePage() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("returned to home page");
    }

    public void changeQuantityTo(int quantity) {
        protocol.changeQuantityTo(quantity);
        log.fine("changed quantity to " + quantity);
    }

    public void removeFirstItem() {
        protocol.removeFirstItemFromCart();
        log.fine("removed first cart item");
    }

    public void assertBadgeShowsCount(int expected) {
        protocol.waitForCartCountToBe(expected);
        assertThat(protocol.getCartState().itemCount())
            .as("cart badge should show " + expected + " item(s)")
            .isEqualTo(expected);
        log.fine("cart badge shows " + expected + " item(s)");
    }

    public void assertBadgeIncreasedBy(int delta) {
        int expected = notedItemCount + delta;
        protocol.waitForCartCountToBe(expected);
        assertThat(protocol.getCartState().itemCount())
            .as("cart badge should have increased by " + delta)
            .isEqualTo(expected);
        log.fine("cart badge increased by " + delta + " to " + expected);
    }

    public void assertCartContainsAtLeastItems(int minimum) {
        protocol.waitForCartItemsToAppear();
        assertThat(protocol.getCartState().items().size())
            .as("cart should contain at least " + minimum + " items")
            .isGreaterThanOrEqualTo(minimum);
        log.fine("cart contains at least " + minimum + " items");
    }

    public void assertTotalShowsPrice() {
        var total = protocol.getCartState().total();
        assertThat(total)
            .as("cart total should show a price with $")
            .matches(".*\\$\\d+.*");
        log.fine("cart total shows price: " + total);
    }

    public void assertTotalChanged() {
        protocol.waitForCartTotalToChange(notedTotal);
        assertThat(protocol.getCartState().total())
            .as("cart total should have changed from " + notedTotal)
            .isNotEqualTo(notedTotal);
        log.fine("cart total changed from " + notedTotal);
    }

    public void assertEmptyState() {
        protocol.waitForCartToBeEmpty();
        assertThat(protocol.getCartState().isEmpty())
            .as("cart should show empty state")
            .isTrue();
        log.fine("cart is empty");
    }

    public void assertFirstItemVisible() {
        protocol.waitForCartItemsToAppear();
        var items = protocol.getCartState().items();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(items).as("cart should have at least one item").isNotEmpty();
        soft.assertAll();
        log.fine("first cart item is visible");
    }
}
