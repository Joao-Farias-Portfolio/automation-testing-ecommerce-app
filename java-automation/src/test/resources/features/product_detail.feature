@detail @web
Feature: Product Detail Page
  As an online shopper on Linea Supply
  I want to view detailed product information
  So that I can make an informed purchase decision

  @smoke @detail_navigate
  Scenario: Navigate to a product detail page
    Given the shopper is on the homepage with products loaded
    When the shopper clicks the first product card
    Then the URL should match the product detail pattern
    And the product title should be visible on the detail page

  @detail_content @api
  Scenario: Product detail page shows full information
    Given the shopper is on the homepage with products loaded
    When the shopper clicks the first product card
    Then the product detail page should show price, description and image

  @detail_title_match @api
  Scenario: Product title on detail page matches listing title
    Given the shopper is viewing the first product
    Then the product title should match the one from the listing

  @detail_add_to_cart
  Scenario: Add a product to cart from the detail page
    Given the shopper is viewing the first product
    And the shopper notes the cart count on the detail page
    When the shopper adds the product to the cart from the detail page
    Then the add to cart button should show Added to Cart and be disabled

  @detail_back_nav
  Scenario: Navigate back from the product detail page
    Given the shopper is on the homepage with products loaded
    When the shopper clicks the first product card
    And the shopper navigates back
    Then the shopper should be back on the product listing
