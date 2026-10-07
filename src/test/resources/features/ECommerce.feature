@all @ui
Feature: E-Commerce Product Purchase
  
  Scenario: Search and Add to Cart from Lowest Rated Seller
    Given user navigates to amazon
    When user searches for "cep telefonu"
    And user filters price between "15000" and "20000"
    And user selects a random product from the last row
    And user adds the product to the cart from the seller with the lowest rating
    Then user verifies the product is in the cart
