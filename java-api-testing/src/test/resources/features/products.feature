@products
Feature: Products API
  As a shopper on Linea Supply
  I want to retrieve product information via the API
  So that I can browse and discover products

  @smoke
  Scenario: Products endpoint returns a list of products
    When the API consumer requests all products
    Then the status code should be 200
    And the response should contain at least one product
    And each product should have an id, title, and price

  Scenario: Products are returned with image URLs
    When the API consumer requests all products
    Then the status code should be 200
    And products should include image URL fields

  Scenario: Products can be filtered by category
    Given a valid category ID exists in the system
    When the API consumer requests products for that category
    Then the status code should be 200
    And all returned products should belong to the requested category

  Scenario: Products can be sorted by price ascending
    When the API consumer requests products sorted by price ascending
    Then the status code should be 200
    And products should be returned in ascending price order

  Scenario: Products can be sorted by price descending
    When the API consumer requests products sorted by price descending
    Then the status code should be 200
    And products should be returned in descending price order

  Scenario: Products include delivery summary when requested
    When the API consumer requests products with delivery summary
    Then the status code should be 200
    And products should include delivery summary information

  Scenario: Unknown sort parameter falls back to default ordering
    When the API consumer requests products with an unknown sort order
    Then the status code should be 200
    And the response should contain at least one product
