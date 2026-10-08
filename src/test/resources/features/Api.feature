@all @api
Feature: API Mock Server Testleri
  
  Scenario: Token Alma, Fatura Goruntuleme ve Gonderme Testi
    Given kullanici mock server'dan token alir
    When kullanici "12345" barkodlu faturayi sorgular
    Then basarili fatura sorgusu yaniti dosyaya kaydedilir
    When kullanici "12345" barkodlu faturayi gonderir
    Then basarili fatura gonderme yaniti dosyaya kaydedilir
