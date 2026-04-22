@saved @web
Feature: Save and Wishlist Functionality
  As an online shopper on Linea Supply
  I want to save products I am interested in
  So that I can find them easily later

  @smoke
  Scenario: Toggle save state for a product
    Given the shopper is on the homepage with save buttons visible
    And the shopper records the initial save state of the first product
    When the shopper toggles the save button for the first product
    Then the save state of the first product should have changed
    When the shopper toggles the save button again
    Then the save state should be restored to the initial state

  Scenario: Saved items count is displayed on the saved page
    Given the shopper is on the homepage with save buttons visible
    And the shopper has saved the first product
    When the shopper navigates to the saved page
    Then the saved count should be visible and show a number

  Scenario: Wishlist link is visible on the saved page
    Given the shopper is on the saved page
    Then the wishlist link should be visible
    When the shopper clicks the wishlist link
    Then the URL should contain /saved

  Scenario: Save state persists during navigation within a session
    Given the shopper is on the homepage with save buttons visible
    And the shopper has saved the first product
    When the shopper navigates to the saved page
    And the shopper returns to the homepage
    Then the save button should be visible and functional on the detail page

  Scenario: Save a product from the product detail page
    Given the shopper is on the homepage with products loaded
    When the shopper clicks the first product card
    Then the save button should be visible and functional on the detail page
