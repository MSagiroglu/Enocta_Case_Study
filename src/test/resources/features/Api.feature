@api
Feature: API Mock Server Tests
  
  Scenario: Test Token, View Invoice and Send Invoice
    Given user gets a token from mock server
    When user fetches invoice with barcode "12345"
    Then the invoice response should be saved to file
    When user sends invoice with barcode "12345"
    Then the send invoice response should be saved to file
