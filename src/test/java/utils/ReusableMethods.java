package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReusableMethods {

    private static final int DEFAULT_TIMEOUT = 20;

    // ==========================================
    // CLICK & SEND KEYS METHODS
    // ==========================================
    public static void click(WebElement element, String elementName) {
        try {
            getWait().until(ExpectedConditions.elementToBeClickable(element)).click();
            LoggerUtils.info("'" + elementName + "' elementine tiklandi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine tiklanamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void clickWithJS(WebElement element, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) Driver.getDriver();
            js.executeScript("arguments[0].click();", element);
            LoggerUtils.info("'" + elementName + "' elementine JS ile tiklandi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine JS ile tiklanamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void sendKeys(WebElement element, String text, String elementName) {
        try {
            WebElement visibleElement = getWait().until(ExpectedConditions.visibilityOf(element));
            visibleElement.clear();
            visibleElement.sendKeys(text);
            LoggerUtils.info("'" + elementName + "' alanina '" + text + "' degeri girildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' alanina deger girilemedi: " + e.getMessage());
            throw e;
        }
    }

    public static String getText(WebElement element, String elementName) {
        try {
            String text = getWait().until(ExpectedConditions.visibilityOf(element)).getText();
            LoggerUtils.info("'" + elementName + "' elementinin metni okundu: " + text);
            return text;
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementinin metni okunamadi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // WAIT METHODS
    // ==========================================
    private static WebDriverWait getWait() {
        return new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static void waitForVisibility(WebElement element, String elementName) {
        try {
            getWait().until(ExpectedConditions.visibilityOf(element));
            LoggerUtils.info("'" + elementName + "' elementinin gorunur olmasi beklendi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementi gorunur olmadi: " + e.getMessage());
            throw e;
        }
    }

    public static void waitForAllElements(List<WebElement> elements, String listName) {
        try {
            getWait().until(ExpectedConditions.visibilityOfAllElements(elements));
            LoggerUtils.info("'" + listName + "' listesindeki elementlerin gorunur olmasi beklendi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + listName + "' listesi gorunur olmadi: " + e.getMessage());
            throw e;
        }
    }

    public static void hardWait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            LoggerUtils.info(seconds + " saniye statik bekleme yapildi.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void waitForUrlContains(String text) {
        try {
            getWait().until(ExpectedConditions.urlContains(text));
            LoggerUtils.info("URL'nin '" + text + "' metnini icermesi beklendi (Explicit Wait).");
        } catch (Exception e) {
            LoggerUtils.error("URL '" + text + "' metnini icermedi: " + e.getMessage());
            throw e;
        }
    }

    public static void waitForPageToLoad() {
        try {
            getWait().until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
            LoggerUtils.info("Sayfanin DOM yapisinin tamamen yuklenmesi (readyState=complete) beklendi.");
        } catch (Exception e) {
            LoggerUtils.error("Sayfa tam anlamiyla yuklenemedi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // DROPDOWN METHODS
    // ==========================================
    public static void selectByVisibleText(WebElement element, String text, String dropdownName) {
        try {
            Select select = new Select(element);
            select.selectByVisibleText(text);
            LoggerUtils.info("'" + dropdownName + "' dropdown'undan '" + text + "' secildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + dropdownName + "' dropdown'undan secim yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void selectByIndex(WebElement element, int index, String dropdownName) {
        try {
            Select select = new Select(element);
            select.selectByIndex(index);
            LoggerUtils.info("'" + dropdownName + "' dropdown'undan " + index + ". index secildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + dropdownName + "' dropdown'undan secim yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void selectByValue(WebElement element, String value, String dropdownName) {
        try {
            Select select = new Select(element);
            select.selectByValue(value);
            LoggerUtils.info("'" + dropdownName + "' dropdown'undan '" + value + "' degeri secildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + dropdownName + "' dropdown'undan secim yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // ALERT METHODS
    // ==========================================
    public static void acceptAlert() {
        try {
            getWait().until(ExpectedConditions.alertIsPresent());
            Driver.getDriver().switchTo().alert().accept();
            LoggerUtils.info("Alert (Uyari) kabul edildi.");
        } catch (Exception e) {
            LoggerUtils.error("Alert kabul edilemedi: " + e.getMessage());
            throw e;
        }
    }

    public static void dismissAlert() {
        try {
            getWait().until(ExpectedConditions.alertIsPresent());
            Driver.getDriver().switchTo().alert().dismiss();
            LoggerUtils.info("Alert (Uyari) reddedildi.");
        } catch (Exception e) {
            LoggerUtils.error("Alert reddedilemedi: " + e.getMessage());
            throw e;
        }
    }

    public static String getAlertText() {
        try {
            getWait().until(ExpectedConditions.alertIsPresent());
            String text = Driver.getDriver().switchTo().alert().getText();
            LoggerUtils.info("Alert metni okundu: " + text);
            return text;
        } catch (Exception e) {
            LoggerUtils.error("Alert metni okunamadi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // IFRAME METHODS
    // ==========================================
    public static void switchToIframeByIndex(int index) {
        try {
            getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
            LoggerUtils.info(index + ". indexli iframe'e gecis yapildi.");
        } catch (Exception e) {
            LoggerUtils.error(index + ". indexli iframe'e gecis yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void switchToIframeByElement(WebElement iframeElement, String iframeName) {
        try {
            getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeElement));
            LoggerUtils.info("'" + iframeName + "' iframe'ine gecis yapildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + iframeName + "' iframe'ine gecis yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void switchToDefaultContent() {
        try {
            Driver.getDriver().switchTo().defaultContent();
            LoggerUtils.info("Ana sayfa icerigine (Default Content) geri donuldu.");
        } catch (Exception e) {
            LoggerUtils.error("Ana sayfaya geri donulemedi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // WINDOW / TAB METHODS
    // ==========================================
    public static void switchToWindowByTitle(String targetTitle) {
        try {
            Set<String> windowHandles = Driver.getDriver().getWindowHandles();
            for (String handle : windowHandles) {
                Driver.getDriver().switchTo().window(handle);
                if (Driver.getDriver().getTitle().contains(targetTitle)) {
                    LoggerUtils.info("'" + targetTitle + "' baslikli sekmeye/pencereye gecildi.");
                    return;
                }
            }
            throw new Exception("Belirtilen basliga sahip pencere bulunamadi.");
        } catch (Exception e) {
            LoggerUtils.error("Pencere gecisi basarisiz: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void switchToWindowByIndex(int index) {
        try {
            List<String> windowHandles = new ArrayList<>(Driver.getDriver().getWindowHandles());
            Driver.getDriver().switchTo().window(windowHandles.get(index));
            LoggerUtils.info(index + ". siradaki sekmeye/pencereye gecildi.");
        } catch (Exception e) {
            LoggerUtils.error(index + ". siradaki sekmeye/pencereye gecilemedi: " + e.getMessage());
            throw e;
        }
    }

    // ==========================================
    // SCROLL METHODS
    // ==========================================
    public static void scrollToElement(WebElement element, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) Driver.getDriver();
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});", element);
            LoggerUtils.info("Ekranda '" + elementName + "' elementine kaydirildi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine kaydirilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void scrollToBottom() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) Driver.getDriver();
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            LoggerUtils.info("Sayfanin en altina kaydirildi.");
        } catch (Exception e) {
            LoggerUtils.error("Sayfanin altina kaydirma basarisiz: " + e.getMessage());
        }
    }

    // ==========================================
    // ACTIONS (MOUSE & KEYBOARD) METHODS
    // ==========================================
    public static void hoverOverElement(WebElement element, String elementName) {
        try {
            Actions actions = new Actions(Driver.getDriver());
            actions.moveToElement(element).perform();
            LoggerUtils.info("Fare '" + elementName + "' elementinin uzerine getirildi (Hover).");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine hover yapilamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void dragAndDrop(WebElement source, WebElement target, String sourceName, String targetName) {
        try {
            Actions actions = new Actions(Driver.getDriver());
            actions.dragAndDrop(source, target).perform();
            LoggerUtils.info("'" + sourceName + "' elementi '" + targetName + "' hedefine suruklendi.");
        } catch (Exception e) {
            LoggerUtils.error("Surukle-Birak islemi basarisiz oldu: " + e.getMessage());
            throw e;
        }
    }

    public static void doubleClick(WebElement element, String elementName) {
        try {
            Actions actions = new Actions(Driver.getDriver());
            actions.doubleClick(element).perform();
            LoggerUtils.info("'" + elementName + "' elementine cift tiklandi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine cift tiklanamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void rightClick(WebElement element, String elementName) {
        try {
            Actions actions = new Actions(Driver.getDriver());
            actions.contextClick(element).perform();
            LoggerUtils.info("'" + elementName + "' elementine sag tiklandi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementine sag tiklanamadi: " + e.getMessage());
            throw e;
        }
    }
}
