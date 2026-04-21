@delivery-options
Feature: Delivery Options API
  As a shopper on Linea Supply
  I want to retrieve delivery options via the API
  So that I can filter products by delivery speed

  @smoke
  Scenario: Delivery options endpoint returns active options
    When the API consumer requests all delivery options
    Then the status code should be 200
    And the response should contain at least one delivery option
    And each delivery option should have a name and price

  Scenario: Delivery options include estimated delivery times
    When the API consumer requests all delivery options
    Then the status code should be 200
    And each delivery option should include estimated days range

  Scenario: Delivery options can be used to filter products
    Given a valid delivery option ID exists in the system
    When the API consumer requests products for that delivery option
    Then the status code should be 200
    And the response should contain at least one product
