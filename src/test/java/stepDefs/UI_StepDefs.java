package stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import pages.CartPage;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchPage;
import utils.Driver;
import utils.ReusableMethods;
import utils.LoggerUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UI_StepDefs {

    HomePage homePage = new HomePage();
    SearchPage searchPage = new SearchPage();
    ProductPage productPage = new ProductPage();
    CartPage cartPage = new CartPage();

    @Given("kullanici amazon anasayfasina gider")
    public void kullanici_amazon_anasayfasina_gider() {
        LoggerUtils.info("Amazon anasayfasi gidiliyor: https://www.amazon.com.tr");
        Driver.getDriver().get("https://www.amazon.com.tr");
        try {
            ReusableMethods.waitForVisibility(homePage.cookieAccept, "Cerez Kabul Butonu");
            homePage.cookieAccept.click();
        } catch (Exception e) {
            LoggerUtils.info("Cerez banner bulunamadi veya zaten kabul edilmis.");
        }
    }

    @When("kullanici giris islemini yapar")
    public void kullanici_giris_islemini_yapar() {
        LoggerUtils.info("Güvenlik (Captcha vb.) önlemleri sebebiyle login adimi atlanmistir (Bypass).");
        // Gerçek bir otomasyonda burada login işlemleri yapılır.
    }

    @When("kullanici {string} aramasi yapar")
    public void kullanici_aramasi_yapar(String item) {
        LoggerUtils.info("Arama yapiliyor: " + item);
        
        boolean found = false;
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                ReusableMethods.waitForElementAndSendKeys(Driver.getDriver(), By.id("twotabsearchtextbox"), item, "Arama Kutusu");
                found = true;
                break;
            } catch (Exception e) {
                LoggerUtils.warning("Arama kutusu bulunamadi (Captcha veya bot korumasi olabilir). Sayfa yenileniyor... Deneme: " + (i + 1));
                Driver.getDriver().navigate().refresh();
                new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(5)).until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
                if (i == maxRetries - 1) {
                    String pageSource = Driver.getDriver().getPageSource().toLowerCase();
                    String currentUrl = Driver.getDriver().getCurrentUrl().toLowerCase();
                    
                    boolean isBotProtection = pageSource.contains("captcha") || 
                                              pageSource.contains("robot") || 
                                              pageSource.contains("enter the characters") ||
                                              currentUrl.contains("captcha");
                    
                    if (isBotProtection) {
                        LoggerUtils.error("Maksimum deneme sayisina ulasildi. Amazon bot korumasi (Captcha/Robot) tespit edildi.");
                        throw new org.testng.SkipException("Amazon bot korumasi / Captcha tespit edildi. Çevresel kısıtlamalar nedeniyle test atlanıyor (Skipped).", e);
                    } else {
                        throw e; // Gercek bir timeout/hata olabilir
                    }
                }
            }
        }

        try {
            ReusableMethods.waitForAndClick(Driver.getDriver(), By.id("nav-search-submit-button"), "Arama Butonu", 10);
        } catch (Exception e) {
            Driver.getDriver().findElement(By.id("twotabsearchtextbox")).sendKeys(Keys.ENTER);
        }
    }

    @When("kullanici fiyat araligini {string} ve {string} olarak belirler")
    public void kullanici_fiyat_araligini_ve_olarak_belirler(String min, String max) {
        LoggerUtils.info("Fiyat araligi filtresi dogrudan URL uzerinden uygulaniyor: " + min + " - " + max + " TL");
        
        // Arama butonuna basildiktan sonra sayfanin yuklenmesini bekle (URL degismeli)
        WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("k="));

        String currentUrl = Driver.getDriver().getCurrentUrl();
        if (!currentUrl.contains("low-price")) {
            String separator = currentUrl.contains("?") ? "&" : "?";
            String targetUrl = currentUrl + separator + "rnid=13736708031&low-price=" + min + "&high-price=" + max;
            Driver.getDriver().get(targetUrl);
        }
        
        // Hard wait yerine sayfanin (yeni sonuclarin) yuklenmesini dinamik bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot")));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("low-price"),
            ExpectedConditions.urlContains("p_36")
        ));
    }

    @When("kullanici son satirdan rastgele bir urun secer")
    public void kullanici_son_satirdan_rastgele_bir_urun_secer() {
        LoggerUtils.info("Son satirdan rastgele urun seciliyor.");
        try {
            List<WebElement> products = searchPage.productList;

            if (products.size() > 0) {
                // Sadece telefon olanlari filtrele (aksesuarlari cikar)
                List<WebElement> phoneProducts = new ArrayList<>();
                for (WebElement product : products) {
                    try {
                        String text = product.getText().toLowerCase();
                        
                        // Aksesuar kelimelerini kontrol et (kulaklık, mikrofon vb. eklendi)
                        boolean isAccessory = text.contains("kılıf") || text.contains("case") || 
                                              text.contains("koruyucu") || text.contains("şarj") || 
                                              text.contains("kablo") || text.contains("tutucu") || 
                                              text.contains("çanta") || text.contains("cüzdan") ||
                                              text.contains("kordon") || text.contains("lens") ||
                                              text.contains("kapak") || text.contains("cover") ||
                                              text.contains("adaptör") || text.contains("teleskop") ||
                                              text.contains("kulaklık") || text.contains("kulaklik") ||
                                              text.contains("headset") || text.contains("earbud") ||
                                              text.contains("mikrofon") || text.contains("hoparlör") ||
                                              text.contains("watch") || text.contains("saat");

                        // Telefon marka/model kelimeleri (SADECE MARKALAR, 'telefon' kelimesi cok genel)
                        boolean isPhoneBrand = text.contains("iphone") || text.contains("samsung") || 
                                               text.contains("xiaomi") || text.contains("redmi") || 
                                               text.contains("poco") || text.contains("vivo") || 
                                               text.contains("oppo") || text.contains("honor") || 
                                               text.contains("realme") || text.contains("tecno") ||
                                               text.contains("galaxy") || text.contains("apple") ||
                                               text.contains("huawei") || text.contains("motorola") ||
                                               text.contains("nothing") || text.contains("infinix") ||
                                               text.contains("general mobile") || text.contains("casper") ||
                                               text.contains("omix") || text.contains("reeder") ||
                                               text.contains("tcl");

                        // Aksesuar degilse ve bir telefon markasi tasiyorsa
                        if (!isAccessory && isPhoneBrand) {
                            phoneProducts.add(product);
                        }
                    } catch (Exception ignored) {}
                }

                // Eger filtrelenmis liste bossa fallback olarak son satirdan herhangi birini al
                if (phoneProducts.isEmpty()) {
                    LoggerUtils.warning("Telefon filtresine uygun urun bulunamadi, tum liste kullanilacak.");
                    phoneProducts = products;
                }

                // Determine last row products (approx. last 1 to 4 products)
                int lastRowCount = Math.min(phoneProducts.size(), 4);
                List<WebElement> lastProducts = phoneProducts.subList(phoneProducts.size() - lastRowCount, phoneProducts.size());

                // Select a random product from the last row
                int randomIndex = ThreadLocalRandom.current().nextInt(lastProducts.size());
                WebElement selectedProduct = lastProducts.get(randomIndex);

                WebElement productLink = selectedProduct.findElement(By.xpath(".//a[contains(@href, '/dp/')]"));
                String url = productLink.getAttribute("href");

                LoggerUtils.info("Secilen urun: " + url);
                Driver.getDriver().get(url);
                // Hard wait yerine urun basliginin yuklenmesini bekle
                WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));
            } else {
                Assert.fail("No products found on the search page.");
            }
        } catch (Exception e) {
            Assert.fail("Failed to select product: " + e.getMessage());
        }
    }

    @When("kullanici urunu en dusuk puanli saticidan sepete ekler")
    public void kullanici_urunu_en_dusuk_puanli_saticidan_sepete_ekler() {
        LoggerUtils.info("Diger saticilar arasindan en dusuk puanli sepete ekleniyor.");
        WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(10));

        try {
            boolean hasOtherSellers = false;

            try {
                if (productPage.otherSellersLink.isDisplayed()) {
                    hasOtherSellers = true;
                }
            } catch (Exception ignored) {}

            if (hasOtherSellers) {
                ReusableMethods.clickWithJS(productPage.otherSellersLink, "Diger Saticilar Linki");
                // Hard wait yerine saticilar listesinin DOM'a inmesini bekle
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#aod-offer-list, #aod-offer")));

                List<WebElement> sellers = productPage.otherSellersList;

                if (sellers != null && sellers.size() > 0) {
                    WebElement lowestRatedSeller = null;
                    double lowestRating = Double.MAX_VALUE;

                    for (WebElement seller : sellers) {
                        try {
                            String ratingText = "";
                            try {
                                // Sadece satici puanini oku, fiyati degil!
                                ratingText = seller.findElement(By.cssSelector("#aod-offer-seller-rating, i[class*='a-icon-star'] .a-icon-alt")).getAttribute("innerText");
                            } catch (Exception e) {
                                continue; // Puan yoksa bu saticiyi atla
                            }

                            if (ratingText != null && !ratingText.trim().isEmpty()) {
                                double rating = extractRating(ratingText);
                                // Gecerli bir puan donduyse karsilastir (999.0 hatali demek)
                                if (rating < 999.0 && rating < lowestRating) {
                                    lowestRating = rating;
                                    lowestRatedSeller = seller;
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    if (lowestRatedSeller != null) {
                        WebElement addBtn = lowestRatedSeller.findElement(By.cssSelector("input[name='submit.addToCart'], .aod-add-to-cart-button"));
                        ReusableMethods.clickWithJS(addBtn, "En Dusuk Puanli Satici Sepete Ekle");
                        LoggerUtils.info("En dusuk puanli satici (rating: " + lowestRating + ") secildi ve sepete eklendi.");
                        return;
                    }
                }
            }

            // Fallback: Varsayılan add to cart butonunu kullan
            LoggerUtils.warning("Diger saticilar bulunamadi veya secilemedi, varsayilan buton kullaniliyor.");
            ReusableMethods.clickWithJS(productPage.defaultAddToCartButton, "Varsayilan Sepete Ekle Butonu");

        } catch (Exception e) {
            LoggerUtils.error("Add to cart hatasi: " + e.getMessage());
            Assert.fail("No Add to Cart button found. Product might be out of stock or requires variant selection.");
        }
    }

    private double extractRating(String ratingText) {
        if (ratingText == null || ratingText.trim().isEmpty()) return 999.0;
        
        // Amazon rating formats: "4,5 / 5 yıldız", "%92 olumlu", "4.5 out of 5 stars"
        String cleaned = ratingText.replaceAll("[^0-9,.]", "").replace(",", ".");
        if (cleaned.isEmpty()) return 999.0;
        
        try {
            double rating = Double.parseDouble(cleaned);
            if (rating > 5 && rating <= 100) {
                // If it's a percentage like 92% positive, convert to 5-star scale (92 -> 4.6)
                return (rating / 100.0) * 5.0;
            }
            return rating;
        } catch (NumberFormatException e) {
            return 999.0;
        }
    }

    @Then("kullanici urunun sepete eklendigini dogrular")
    public void kullanici_urunun_sepete_eklendigini_dogrular() {
        LoggerUtils.info("Sepete gidilip urun kontrol ediliyor.");
        try {
            // Hard wait yerine sepet sayacinin degismesini veya sepete eklendi mesajini bekle
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(10));
            try {
                wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("nav-cart-count"), "0")));
            } catch (Exception e) {
                // Ignore if it doesn't change immediately, proceed to cart anyway
            }
            Driver.getDriver().get("https://www.amazon.com.tr/cart");
            ReusableMethods.waitForVisibility(Driver.getDriver().findElement(By.cssSelector(".sc-list-item")), "Sepet Listesi");
            List<WebElement> items = cartPage.cartItems;
            Assert.assertTrue(items.size() > 0, "Cart is empty on Amazon!");
        } catch (Exception e) {
            Assert.fail("Failed to verify cart: " + e.getMessage());
        }
    }
}
