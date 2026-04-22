@delivery @web
Feature: Delivery Options
  As an online shopper on Linea Supply
  I want to choose a delivery option for a product
  So that I can select the shipping method that suits me

  Background:
    Given the shopper has navigated to a product detail page

  @smoke
  Scenario: Delivery options are displayed on the product detail page
    Then the delivery section should be visible
    And the delivery section should contain radio button options

  Scenario: One delivery option is pre-selected by default
    Then the delivery section should be visible
    And one delivery option should be selected by default

  Scenario: Shopper can select a different delivery option
    Then the delivery section should contain radio button options
    When the shopper selects a different delivery option
    Then a different delivery option should now be selected

  Scenario: Delivery section has a descriptive header
    Then the delivery section should have a header with delivery options text

  Scenario: No minimum order restrictions are shown
    Then no minimum order restrictions should be shown

  Scenario: Product page works correctly regardless of delivery options
    Then the product detail page should still be functional without delivery options
