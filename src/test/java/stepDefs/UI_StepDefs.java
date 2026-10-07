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
import java.util.concurrent.ThreadLocalRandom;

public class UI_StepDefs {

    HomePage homePage = new HomePage();
    SearchPage searchPage = new SearchPage();
    ProductPage productPage = new ProductPage();
    CartPage cartPage = new CartPage();

    @Given("user navigates to amazon")
    public void user_navigates_to_amazon() {
        LoggerUtils.info("Amazon anasayfasi gidiliyor: https://www.amazon.com.tr");
        Driver.getDriver().get("https://www.amazon.com.tr");
        try {
            ReusableMethods.waitForVisibility(homePage.cookieAccept, "Cerez Kabul Butonu");
            homePage.cookieAccept.click();
        } catch (Exception e) {
            LoggerUtils.info("Cerez banner bulunamadi veya zaten kabul edilmis.");
        }
    }

    @When("user searches for {string}")
    public void user_searches_for(String item) {
        LoggerUtils.info("Arama yapiliyor: " + item);
        ReusableMethods.waitForElementAndSendKeys(Driver.getDriver(), By.id("twotabsearchtextbox"), item, "Arama Kutusu");
        try {
            ReusableMethods.waitForAndClick(Driver.getDriver(), By.id("nav-search-submit-button"), "Arama Butonu", 10);
        } catch (Exception e) {
            homePage.searchBox.sendKeys(Keys.ENTER);
        }
    }

    @When("user filters price between {string} and {string}")
    public void user_filters_price_between_and(String min, String max) {
        LoggerUtils.info("Fiyat araligi filtresi dogrudan URL uzerinden uygulaniyor: " + min + " - " + max + " TL");
        
        // Arama butonuna basildiktan sonra sayfanin yuklenmesini bekle (URL degismeli)
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(Driver.getDriver(), java.time.Duration.ofSeconds(10));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("k="));

        String currentUrl = Driver.getDriver().getCurrentUrl();
        String separator = currentUrl.contains("?") ? "&" : "?";
        Driver.getDriver().get(currentUrl + separator + "low-price=" + min + "&high-price=" + max);
        ReusableMethods.hardWait(3);
    }

    @When("user selects a random product from the last row")
    public void user_selects_a_random_product_from_the_last_row() {
        LoggerUtils.info("Son satirdan rastgele urun seciliyor.");
        try {
            List<WebElement> products = searchPage.productList;

            if (products.size() > 0) {
                // Sadece telefon olanlari filtrele (aksesuarlari cikar)
                java.util.List<WebElement> phoneProducts = new java.util.ArrayList<>();
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
                ReusableMethods.hardWait(2);
            } else {
                Assert.fail("No products found on the search page.");
            }
        } catch (Exception e) {
            Assert.fail("Failed to select product: " + e.getMessage());
        }
    }

    @When("user adds the product to the cart from the seller with the lowest rating")
    public void user_adds_the_product_to_the_cart_from_the_seller_with_the_lowest_rating() {
        LoggerUtils.info("Diger saticilar arasindan en dusuk puanli sepete ekleniyor.");

        try {
            ReusableMethods.hardWait(2);
            boolean hasOtherSellers = false;

            try {
                if (productPage.otherSellersLink.isDisplayed()) {
                    hasOtherSellers = true;
                }
            } catch (Exception ignored) {}

            if (hasOtherSellers) {
                ReusableMethods.clickWithJS(productPage.otherSellersLink, "Diger Saticilar Linki");
                ReusableMethods.hardWait(3);

                List<WebElement> sellers = productPage.otherSellersList;

                if (sellers != null && sellers.size() > 0) {
                    WebElement lowestRatedSeller = null;
                    double lowestRating = Double.MAX_VALUE;

                    for (WebElement seller : sellers) {
                        try {
                            String ratingText = "";
                            try {
                                ratingText = seller.findElement(By.cssSelector(".aod-price .a-offscreen, #aod-offer-seller-rating")).getText();
                            } catch (Exception e) {
                                // Ignore if rating is missing
                            }

                            double rating = extractRating(ratingText);

                            if (rating < lowestRating) {
                                lowestRating = rating;
                                lowestRatedSeller = seller;
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

    @Then("user verifies the product is in the cart")
    public void user_verifies_the_product_is_in_the_cart() {
        LoggerUtils.info("Sepete gidilip urun kontrol ediliyor.");
        try {
            ReusableMethods.hardWait(3);
            Driver.getDriver().get("https://www.amazon.com.tr/cart");
            ReusableMethods.waitForVisibility(Driver.getDriver().findElement(By.cssSelector(".sc-list-item")), "Sepet Listesi");
            List<WebElement> items = cartPage.cartItems;
            Assert.assertTrue(items.size() > 0, "Cart is empty on Amazon!");
        } catch (Exception e) {
            Assert.fail("Failed to verify cart: " + e.getMessage());
        }
    }
}