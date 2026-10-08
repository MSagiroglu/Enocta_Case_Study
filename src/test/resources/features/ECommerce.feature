@all @ui
Feature: E-Ticaret Urun Satin Alma
  
  Scenario: Arama Yapma ve En Dusuk Puanli Saticidan Sepete Ekleme
    Given kullanici amazon anasayfasina gider
    When kullanici giris islemini yapar
    And kullanici "cep telefonu" aramasi yapar
    And kullanici fiyat araligini "15000" ve "20000" olarak belirler
    And kullanici son satirdan rastgele bir urun secer
    And kullanici urunu en dusuk puanli saticidan sepete ekler
    Then kullanici urunun sepete eklendigini dogrular
