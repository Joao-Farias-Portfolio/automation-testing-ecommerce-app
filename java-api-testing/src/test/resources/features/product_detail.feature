@product-detail
Feature: Product Detail API
  As a shopper on Linea Supply
  I want to retrieve full product details via the API
  So that I can view product information and delivery options

  Background:
    Given a valid product ID exists in the system

  @smoke
  Scenario: Product detail endpoint returns full product information
    When the API consumer requests that product by ID
    Then the status code should be 200
    And the product should have a title, description, and price

  Scenario: Product detail includes delivery options
    When the API consumer requests that product by ID
    Then the status code should be 200
    And the product should include delivery options

  Scenario: Product detail returns category information
    When the API consumer requests that product by ID
    Then the status code should be 200
    And the product should include category information

  Scenario: Requesting a non-existent product returns 404
    When the API consumer requests a product with ID 999999
    Then the status code should be 404

  Scenario: Product image endpoint serves an image
    When the API consumer requests the image for that product
    Then the status code should be 200
    And the response content type should be an image type
