@ui
Feature: E-Commerce Product Purchase
  
  Scenario: Search, Sort by Price, and Verify Cart
    Given user navigates to amazon
    When user searches for "cep telefonu"
    And user filters price between "15000" and "20000"
    And user sorts the results by lowest price
    And user selects the lowest priced product
    And user adds the product to the cart from the seller with the lowest rating
    Then user verifies the selected product title and price match the cart
