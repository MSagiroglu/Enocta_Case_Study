package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ReusableMethods {

    // Varsayilan bekleme suresi
    private static final int DEFAULT_TIMEOUT = 15;

    public static void click(WebElement element, String elementName) {
        try {
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
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
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
            WebElement visibleElement = wait.until(ExpectedConditions.visibilityOf(element));
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
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
            String text = wait.until(ExpectedConditions.visibilityOf(element)).getText();
            LoggerUtils.info("'" + elementName + "' elementinin metni okundu: " + text);
            return text;
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementinin metni okunamadi: " + e.getMessage());
            throw e;
        }
    }

    public static void waitForVisibility(WebElement element, String elementName) {
        try {
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
            wait.until(ExpectedConditions.visibilityOf(element));
            LoggerUtils.info("'" + elementName + "' elementinin gorunur olmasi beklendi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + elementName + "' elementi gorunur olmadi: " + e.getMessage());
            throw e;
        }
    }

    public static void waitForAllElements(List<WebElement> elements, String listName) {
        try {
            WebDriverWait wait = new WebDriverWait(Driver.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
            wait.until(ExpectedConditions.visibilityOfAllElements(elements));
            LoggerUtils.info("'" + listName + "' listesindeki elementlerin gorunur olmasi beklendi.");
        } catch (Exception e) {
            LoggerUtils.error("'" + listName + "' listesi gorunur olmadi: " + e.getMessage());
            throw e;
        }
    }
    
    public static void hardWait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            LoggerUtils.info(seconds + " saniye statik bekleme (hard wait) yapildi.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
