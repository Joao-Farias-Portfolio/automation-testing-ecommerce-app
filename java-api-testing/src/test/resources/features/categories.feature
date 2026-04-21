@categories
Feature: Categories API
  As a shopper on Linea Supply
  I want to retrieve product categories via the API
  So that I can filter products by category

  @smoke
  Scenario: Categories endpoint returns available categories
    When the API consumer requests all categories
    Then the status code should be 200
    And the response should contain at least one category
    And each category should have an id and name

  Scenario: Only categories with products are returned
    When the API consumer requests all categories
    Then the status code should be 200
    And each returned category should have at least one product associated
