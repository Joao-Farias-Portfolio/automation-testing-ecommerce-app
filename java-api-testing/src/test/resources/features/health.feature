@health
Feature: Health API
  As a system operator
  I want to verify the API is running and healthy
  So that I can confirm the service is available

  @smoke
  Scenario: Health endpoint returns healthy status
    When the API consumer checks the health endpoint
    Then the status code should be 200
    And the response body should contain "healthy"
