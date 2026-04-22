@cart
Feature: Cart Functionality
  As an online shopper on Linea Supply
  I want to manage items in my cart
  So that I can purchase the products I need

  Background:
    Given the shopper is on the homepage with products loaded

  @smoke
  Scenario: Add a product to the cart
    Given the shopper notes the current cart count
    When the shopper adds the product to the cart
    Then the cart badge should have increased by 1

  Scenario: Cart persists across page navigation
    Given the shopper notes the current cart count
    When the shopper adds the product to the cart
    And the shopper navigates to the cart page
    And the shopper returns to the homepage
    Then the cart badge should have increased by 1

  Scenario: Cart shows items and total price
    When the shopper adds the product to the cart
    And the shopper navigates to the cart page
    Then the first cart item should be visible
    And the cart total should be visible and show a price

  Scenario: Change item quantity in the cart
    When the shopper adds the product to the cart
    And the shopper navigates to the cart page
    And the shopper notes the current cart total
    And the shopper changes the quantity to 2
    Then the cart total should have changed

  Scenario: Remove an item from the cart
    When the shopper adds the product to the cart
    And the shopper navigates to the cart page
    And the shopper removes the first cart item
    Then the cart should show an empty state

  Scenario: Add multiple different products to the cart
    Given the shopper notes the current cart count
    When the shopper adds the product to the cart
    And the shopper adds the product to the cart
    Then the cart badge should have increased by 2
    When the shopper navigates to the cart page
    Then the cart should contain at least 2 items
