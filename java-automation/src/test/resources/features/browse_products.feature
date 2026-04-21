@browse
Feature: Product Browsing
  As an online shopper on Linea Supply
  I want to see products on the homepage
  So that I can discover items to buy

  @smoke
  Scenario: Products are displayed on the homepage
    Given the homepage has loaded with products
    Then product cards should be visible
    And each product card should show a title and price

  Scenario: Product images are served from the backend
    Given the homepage has loaded with products
    Then product images should have valid sources

  Scenario: Loading state resolves before products appear
    Given the shopper is on the homepage
    Then the page should show a loading indicator briefly
    And product cards should be visible
