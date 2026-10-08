package hooks;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.Driver;
import java.time.Duration;
import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Hooks {
    
    @Before("@ui")
    public void setUp(Scenario scenario) {
        // Selenium loglarini kapat (CDP Version uyari kirliligini onler)
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        
        String browser = Driver.getBrowserName() != null ? Driver.getBrowserName().toUpperCase(Locale.ENGLISH) : "CHROME";
        scenario.log("TESTING ON BROWSER: " + browser);
        
        // Allure raporunda testleri tarayiciya gore ayirmak (farkli parametrelerle kosulmus gibi gostermek) icin
        io.qameta.allure.Allure.parameter("Browser", browser);
        io.qameta.allure.Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(testResult.getName() + " [" + browser + "]");
            testResult.setHistoryId(testResult.getHistoryId() + browser);
        });
        
        utils.LoggerUtils.info("UI Testleri basliyor: " + browser + " tarayicisi baslatiliyor...");
        Driver.getDriver().manage().window().maximize();
    }

    @After("@ui")
    public void tearDown(Scenario scenario) {
        String browser = Driver.getBrowserName() != null ? Driver.getBrowserName().toUpperCase(Locale.ENGLISH) : "CHROME";
        if (scenario.isFailed()) {
            utils.LoggerUtils.error(browser + " tarayicisinda test HATA aldi! Ekran goruntusu aliniyor...");
            final byte[] screenshot = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
        } else if (scenario.getStatus() == io.cucumber.java.Status.SKIPPED) {
            utils.LoggerUtils.warning(browser + " tarayicisinda test ATLANDI (Skipped). Ekran goruntusu ve HTML kaynagi aliniyor...");
            try {
                final byte[] screenshot = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName() + "_skipped");
                String pageSource = Driver.getDriver().getPageSource();
                scenario.attach(pageSource.getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/html", "PageSource");
            } catch (Exception ignored) {}
        } else {
            utils.LoggerUtils.success(browser + " tarayicisinda test BASARIYLA sonuclandi.");
        }
        utils.LoggerUtils.info(browser + " tarayicisi kapatiliyor...");
        Driver.closeDriver();
    }
    
    @Before("@api")
    public void setUpApi(Scenario scenario) {
        io.qameta.allure.Allure.parameter("Type", "API Test");
        io.qameta.allure.Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(testResult.getName() + " [API]");
            testResult.setHistoryId(testResult.getHistoryId() + "API");
        });
        utils.LoggerUtils.info("API Testleri basliyor: " + scenario.getName());
    }
    
    @After("@api")
    public void tearDownApi(Scenario scenario) {
        if (scenario.isFailed()) {
            utils.LoggerUtils.error("API Testi HATA aldi: " + scenario.getName());
        } else {
            utils.LoggerUtils.success("API Testleri BASARIYLA sonuclandi: " + scenario.getName());
        }
    }
}
