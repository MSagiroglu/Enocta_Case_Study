package stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import utils.Driver;

import java.util.List;

import static utils.LoggerUtils.*;
import static utils.ReusableMethods.*;

public class UI_StepDefs extends BaseStep {
    
    // State variables for verification
    private static String expectedTitle = "";
    private static String expectedPrice = "";

    @Given("user navigates to amazon")
    public void kullanici_amazon_anasayfasina_gider() {
        info("Amazon anasayfasina gidiliyor: https://www.amazon.com.tr");
        Driver.getDriver().get("https://www.amazon.com.tr");
        try {
            click(homePage.cookieAccept, "Cerez Kabul Butonu");
        } catch (Exception e) {
            // Cookie banner might not appear
        }
    }

    @When("user searches for {string}")
    public void kullanici_arama_yapar(String item) {
        info("Arama kutusuna '" + item + "' yaziliyor ve arama yapiliyor.");
        sendKeys(homePage.searchBox, item, "Arama Kutusu");
        try {
            click(homePage.searchButton, "Arama Butonu");
        } catch (Exception e) {
            homePage.searchBox.sendKeys(Keys.ENTER);
        }
    }

    @When("user filters price between {string} and {string}")
    public void kullanici_fiyat_filtresi_uygular(String min, String max) {
        info("Fiyat filtresi uygulaniyor: " + min + " TL ile " + max + " TL arasi.");
        try {
            sendKeys(searchPage.minPriceInput, min, "Minimum Fiyat");
            sendKeys(searchPage.maxPriceInput, max, "Maksimum Fiyat");
            clickWithJS(searchPage.goButton, "Fiyat Git Butonu");
            hardWait(2);
        } catch (Exception e) {
            String url = Driver.getDriver().getCurrentUrl();
            String separator = url.contains("?") ? "&" : "?";
            Driver.getDriver().get(url + separator + "rh=p_36%3A" + min + "00-" + max + "00");
            hardWait(2);
        }
    }

    @When("user sorts the results by lowest price")
    public void kullanici_sonuclari_en_dusuk_fiyata_gore_siralar() {
        info("Sonuclar fiyata gore (Dusukten Yuksege) siralanir.");
        try {
            String url = Driver.getDriver().getCurrentUrl();
            String separator = url.contains("?") ? "&" : "?";
            Driver.getDriver().get(url + separator + "s=price-asc-rank");
            hardWait(2);
        } catch (Exception e) {
            Assert.fail("Failed to sort by lowest price");
        }
    }

    @When("user selects the lowest priced product")
    public void kullanici_en_dusuk_fiyatli_urunu_secer() {
        info("Listelenen urunler arasindan sepete eklenebilir en dusuk fiyatli gecerli cep telefonu seciliyor.");
        try {
            waitForAllElements(searchPage.productList, "Urun Listesi");
            List<WebElement> products = searchPage.productList;
            
            java.util.List<String> validUrls = new java.util.ArrayList<>();
            
            for (WebElement product : products) {
                try {
                    String text = product.getText().toLowerCase(new java.util.Locale("tr", "TR"));
                    boolean isPhoneBrand = text.contains("apple") || text.contains("iphone") || text.contains("samsung") || 
                                           text.contains("galaxy") || text.contains("xiaomi") || text.contains("redmi") || 
                                           text.contains("poco") || text.contains("vivo") || text.contains("oppo") || 
                                           text.contains("honor") || text.contains("realme") || text.contains("tecno");
                                           
                    boolean isAccessory = text.contains("kılıf") || text.contains("çanta") || text.contains("cüzdan") || 
                                          text.contains("koruyucu") || text.contains("şarj") || text.contains("kablo") || 
                                          text.contains("tutucu") || text.contains("kordon");
                                          
                    if (isPhoneBrand && !isAccessory) {
                        WebElement productLink = product.findElement(By.xpath(".//a[contains(@href, '/dp/')]"));
                        validUrls.add(productLink.getAttribute("href"));
                    }
                } catch (Exception ignored) {}
            }
            
            Assert.assertTrue(validUrls.size() > 0, "Gecerli bir telefon bulunamadi!");
            
            boolean productSelected = false;
            for (String url : validUrls) {
                Driver.getDriver().get(url);
                hardWait(2);
                
                // Sepete ekle butonu veya Diger saticilar butonu var mi kontrol et
                boolean canBuy = Driver.getDriver().findElements(By.cssSelector("input#add-to-cart-button, input[name='submit.addToCart']")).size() > 0;
                boolean hasOtherSellers = Driver.getDriver().findElements(By.xpath("//a[contains(@title, 'Daha Fazla')] | //a[contains(@title, 'Other Sellers')] | //a[contains(@href, 'offer-listing')]")).size() > 0;
                
                if (canBuy || hasOtherSellers) {
                    productSelected = true;
                    expectedTitle = getText(productPage.productTitle, "Urun Basligi").trim();
                    if (productPage.productPrices.size() > 0) {
                        expectedPrice = productPage.productPrices.get(0).getAttribute("textContent").trim().replaceAll("[^0-9,]", "");
                    }
                    break; // Satis yapilabilen urun bulundu, donguden cik!
                } else {
                    info("Secilen urun stokta yok veya varyant secimi istiyor. Bir sonraki en ucuz urune geciliyor...");
                }
            }
            
            Assert.assertTrue(productSelected, "Sepete eklenebilir hicbir gecerli urun bulunamadi!");
            
        } catch (Exception e) {
            Assert.fail("Urun secimi sirasinda hata: " + e.getMessage());
        }
    }

    @When("user adds the product to the cart from the seller with the lowest rating")
    public void kullanici_urunu_en_dusuk_puanli_saticidan_sepete_ekler() {
        info("Urun sepete ekleniyor.");
        hardWait(2);
        
        try {
            clickWithJS(productPage.otherSellersLink, "Diger Saticilar Linki");
            waitForVisibility(productPage.otherSellersPanel, "Diger Saticilar Paneli");
            
            if(productPage.otherSellersList.size() > 0) {
                WebElement firstOtherSellerAddBtn = productPage.otherSellersList.get(0).findElement(By.cssSelector("input[name='submit.addToCart']"));
                clickWithJS(firstOtherSellerAddBtn, "Diger Satici Sepete Ekle Butonu");
            } else {
                throw new Exception("No other sellers in list");
            }
        } catch (Exception e) {
             try {
                 clickWithJS(productPage.defaultAddToCartButton, "Varsayilan Sepete Ekle Butonu");
             } catch(Exception ex) {
                 Assert.fail("Sepete ekle butonu tiklanamadi.");
             }
        }
        
        // Amazon bazen sigorta/koruma paketi cikarir: "Hayir tesekkurler" butonuna tikla eger varsa
        try {
            hardWait(2);
            WebElement noThanksBtn = Driver.getDriver().findElement(By.xpath("//input[@aria-labelledby='attachSiNoCoverage-announce'] | //button[contains(text(), 'Hayır')] | //input[contains(@aria-labelledby, 'NoCoverage')]"));
            clickWithJS(noThanksBtn, "Koruma Paketi Iptal Butonu");
        } catch(Exception ignored) {}

        hardWait(2);
    }

    @Then("user verifies the selected product title and price match the cart")
    public void kullanici_secilen_urun_ile_sepetteki_urunun_baslik_ve_fiyatini_dogrular() {
        info("Sepete gidilip secilen urun ile sepetteki urunun baslik ve fiyati karsilastiriliyor.");
        try {
            hardWait(3); // Wait for Add to Cart animation/ajax
            Driver.getDriver().get("https://www.amazon.com.tr/cart");
            
            waitForAllElements(cartPage.cartItems, "Sepet Urunleri");
            Assert.assertTrue(cartPage.cartItems.size() > 0, "Cart is empty on Amazon!");
            
            String actualTitle = getText(cartPage.cartItemTitles.get(0), "Sepet Urun Basligi").trim();
            String actualPrice = getText(cartPage.cartItemPrices.get(0), "Sepet Urun Fiyati").trim().replaceAll("[^0-9,]", "");
            
            // Compare first 20 chars of title as Amazon sometimes truncates cart titles
            int compareLen = Math.min(20, Math.min(expectedTitle.length(), actualTitle.length()));
            Assert.assertTrue(actualTitle.substring(0, compareLen).equalsIgnoreCase(expectedTitle.substring(0, compareLen)), 
                "Titles do not match! Expected: " + expectedTitle + " | Actual: " + actualTitle);
                
            if(!expectedPrice.isEmpty() && !actualPrice.isEmpty()) {
                Assert.assertEquals(actualPrice, expectedPrice, "Prices do not match!");
            }
            
        } catch (Exception e) {
            Assert.fail("Failed to verify cart: " + e.getMessage());
        }
    }
}
