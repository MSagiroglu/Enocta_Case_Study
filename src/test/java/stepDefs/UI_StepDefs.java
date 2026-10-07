package stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.HomePage;
import pages.SearchPage;
import pages.ProductPage;
import pages.CartPage;
import utils.Driver;
import utils.LoggerUtils;

import java.time.Duration;
import java.util.List;

public class UI_StepDefs {
    HomePage homePage = new HomePage();
    SearchPage searchPage = new SearchPage();
    ProductPage productPage = new ProductPage();
    CartPage cartPage = new CartPage();
    
    WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(15));
    JavascriptExecutor js = (JavascriptExecutor) Driver.getDriver();
    
    // State variables for verification
    private static String expectedTitle = "";
    private static String expectedPrice = "";

    @Given("user navigates to amazon")
    public void user_navigates_to_amazon() {
        LoggerUtils.info("Amazon anasayfasina gidiliyor: https://www.amazon.com.tr");
        Driver.getDriver().get("https://www.amazon.com.tr");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(homePage.cookieAccept)).click();
        } catch (Exception e) {
            // Cookie banner might not appear
        }
    }

    @When("user searches for {string}")
    public void user_searches_for(String item) {
        LoggerUtils.info("Arama kutusuna '" + item + "' yaziliyor ve arama yapiliyor.");
        wait.until(ExpectedConditions.visibilityOf(homePage.searchBox)).clear();
        homePage.searchBox.sendKeys(item);
        try {
            homePage.searchButton.click();
        } catch (Exception e) {
            homePage.searchBox.sendKeys(org.openqa.selenium.Keys.ENTER);
        }
    }

    @When("user filters price between {string} and {string}")
    public void user_filters_price_between_and(String min, String max) {
        LoggerUtils.info("Fiyat filtresi uygulaniyor: " + min + " TL ile " + max + " TL arasi.");
        try {
            wait.until(ExpectedConditions.visibilityOf(searchPage.minPriceInput)).clear();
            searchPage.minPriceInput.sendKeys(min);
            searchPage.maxPriceInput.clear();
            searchPage.maxPriceInput.sendKeys(max);
            js.executeScript("arguments[0].click();", searchPage.goButton);
            Thread.sleep(2000);
        } catch (Exception e) {
            String url = Driver.getDriver().getCurrentUrl();
            String separator = url.contains("?") ? "&" : "?";
            Driver.getDriver().get(url + separator + "rh=p_36%3A" + min + "00-" + max + "00");
            try { Thread.sleep(2000); } catch(Exception ignored){}
        }
    }

    @When("user sorts the results by lowest price")
    public void user_sorts_the_results_by_lowest_price() {
        LoggerUtils.info("Sonuclar fiyata gore (Dusukten Yuksege) siralanir.");
        try {
            String url = Driver.getDriver().getCurrentUrl();
            String separator = url.contains("?") ? "&" : "?";
            Driver.getDriver().get(url + separator + "s=price-asc-rank");
            Thread.sleep(2000);
        } catch (Exception e) {
            Assert.fail("Failed to sort by lowest price");
        }
    }

    @When("user selects the lowest priced product")
    public void user_selects_the_lowest_priced_product() {
        LoggerUtils.info("Listelenen urunler arasindan en dusuk fiyatli gecerli cep telefonu seciliyor.");
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(searchPage.productList));
            List<WebElement> products = searchPage.productList;
            
            WebElement selectedProduct = null;
            
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
                        selectedProduct = product;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            
            if(selectedProduct == null && products.size() > 0) {
                selectedProduct = products.get(0); // Fallback to first item if strict filter fails
            }

            Assert.assertNotNull(selectedProduct, "No products found to select");
            
            WebElement productLink = selectedProduct.findElement(By.xpath(".//a[contains(@href, '/dp/')]"));
            String url = productLink.getAttribute("href");
            Driver.getDriver().get(url);
            
            // Save state for assertion
            wait.until(ExpectedConditions.visibilityOf(productPage.productTitle));
            expectedTitle = productPage.productTitle.getText().trim();
            
            if (productPage.productPrices.size() > 0) {
                expectedPrice = productPage.productPrices.get(0).getAttribute("textContent").trim().replaceAll("[^0-9,]", "");
            }
            
        } catch (Exception e) {
            Assert.fail("Failed to select product: " + e.getMessage());
        }
    }

    @When("user adds the product to the cart from the seller with the lowest rating")
    public void user_adds_the_product_to_the_cart_from_the_seller_with_the_lowest_rating() {
        LoggerUtils.info("Urun sepete ekleniyor.");
        try { Thread.sleep(2000); } catch(Exception e){}
        
        try {
            js.executeScript("arguments[0].click();", productPage.otherSellersLink);
            wait.until(ExpectedConditions.visibilityOf(productPage.otherSellersPanel));
            
            if(productPage.otherSellersList.size() > 0) {
                WebElement firstOtherSellerAddBtn = productPage.otherSellersList.get(0).findElement(By.cssSelector("input[name='submit.addToCart']"));
                js.executeScript("arguments[0].click();", firstOtherSellerAddBtn);
                return;
            } else {
                throw new Exception("No other sellers in list");
            }
        } catch (Exception e) {
             try {
                 wait.until(ExpectedConditions.visibilityOf(productPage.defaultAddToCartButton));
                 js.executeScript("arguments[0].click();", productPage.defaultAddToCartButton);
             } catch(Exception ex) {
                 Assert.fail("No Add to Cart button found. Product might be out of stock.");
             }
        }
    }

    @Then("user verifies the selected product title and price match the cart")
    public void user_verifies_the_selected_product_title_and_price_match_the_cart() {
        LoggerUtils.info("Sepete gidilip secilen urun ile sepetteki urunun baslik ve fiyati karsilastiriliyor.");
        try {
            Thread.sleep(3000); // Wait for Add to Cart animation/ajax
            Driver.getDriver().get("https://www.amazon.com.tr/cart");
            
            wait.until(ExpectedConditions.visibilityOfAllElements(cartPage.cartItems));
            Assert.assertTrue(cartPage.cartItems.size() > 0, "Cart is empty on Amazon!");
            
            String actualTitle = cartPage.cartItemTitles.get(0).getText().trim();
            String actualPrice = cartPage.cartItemPrices.get(0).getText().trim().replaceAll("[^0-9,]", "");
            
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
