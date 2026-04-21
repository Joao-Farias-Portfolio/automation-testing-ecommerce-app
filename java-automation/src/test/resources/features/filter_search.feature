@filter
Feature: Product Search and Filtering
  As an online shopper on Linea Supply
  I want to search for specific products
  So that I can find what I am looking for quickly

  @smoke
  Scenario: Search for a product by name
    Given the shopper is on the homepage with products visible
    When the shopper searches for the first product name
    Then the URL should contain the search term
    And search results should be displayed

  Scenario: Search with no matching results shows empty state
    Given the shopper is on the homepage with products visible
    When the shopper searches for "xyznonexistentproduct123"
    Then no results or empty state should be shown
