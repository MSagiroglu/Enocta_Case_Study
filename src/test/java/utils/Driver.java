package utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.edge.EdgeDriver;

public class Driver {
    private static ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();
    private static InheritableThreadLocal<String> browserName = new InheritableThreadLocal<>();

    public static void setBrowser(String browser) {
        browserName.set(browser);
    }
    
    public static String getBrowserName() {
        return browserName.get();
    }

    public static WebDriver getDriver() {
        if (driverPool.get() == null) {
            String browser = browserName.get() != null ? browserName.get() : ConfigReader.getProperty("browser");
            switch (browser.toLowerCase()) {
                case "remote-chrome":
                    try {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                        driverPool.set(new RemoteWebDriver(new URL(ConfigReader.getProperty("hub.url")), options));
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
                case "firefox":
                    org.openqa.selenium.firefox.FirefoxOptions firefoxOptions = new org.openqa.selenium.firefox.FirefoxOptions();
                    firefoxOptions.addPreference("dom.webdriver.enabled", false);
                    firefoxOptions.addArguments("--disable-blink-features=AutomationControlled");
                    firefoxOptions.addArguments("-private");
                    driverPool.set(new FirefoxDriver(firefoxOptions));
                    break;
                case "edge":
                    org.openqa.selenium.edge.EdgeOptions edgeOptions = new org.openqa.selenium.edge.EdgeOptions();
                    edgeOptions.addArguments("--remote-allow-origins=*");
                    edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
                    edgeOptions.addArguments("-inprivate");
                    edgeOptions.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
                    driverPool.set(new EdgeDriver(edgeOptions));
                    break;
                case "chrome":
                default:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--remote-allow-origins=*");
                    chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                    chromeOptions.addArguments("--incognito");
                    chromeOptions.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
                    driverPool.set(new ChromeDriver(chromeOptions));
                    break;
            }
            driverPool.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        }
        return driverPool.get();
    }

    public static void closeDriver() {
        if (driverPool.get() != null) {
            driverPool.get().quit();
            driverPool.remove();
        }
    }
}
