package utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.edge.EdgeDriver;
import java.util.Locale;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Driver {
    private static ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();
    private static InheritableThreadLocal<String> browserName = new InheritableThreadLocal<>();

    public static void setBrowser(String browser) {
        browserName.set(browser);
    }
    
    public static String getBrowserName() {
        return browserName.get();
    }

    private static boolean isCI() {
        return "true".equalsIgnoreCase(System.getenv("CI"));
    }

    public static WebDriver getDriver() {
        if (driverPool.get() == null) {
            String browser = browserName.get() != null ? browserName.get() : ConfigReader.getProperty("browser");
            switch (browser.toLowerCase(Locale.ENGLISH)) {
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
                    if (isCI()) {
                        firefoxOptions.addArguments("-headless", "--width=1920", "--height=1080");
                        firefoxOptions.addPreference("general.useragent.override", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                        firefoxOptions.addPreference("dom.webdriver.enabled", false);
                        firefoxOptions.addPreference("useAutomationExtension", false);
                    }
                    driverPool.set(new FirefoxDriver(firefoxOptions));
                    break;
                case "edge":
                    if (!isCI()) {
                        System.setProperty("webdriver.edge.driver", "driver/msedgedriver.exe");
                    }
                    org.openqa.selenium.edge.EdgeOptions edgeOptions = new org.openqa.selenium.edge.EdgeOptions();
                    edgeOptions.addArguments("--remote-allow-origins=*");
                    edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
                    edgeOptions.addArguments("-inprivate");
                    if (isCI()) {
                        edgeOptions.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
                        edgeOptions.addArguments("--disable-gpu");
                        edgeOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    }
                    edgeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
                    edgeOptions.setExperimentalOption("useAutomationExtension", false);
                    driverPool.set(new EdgeDriver(edgeOptions));
                    break;
                case "chrome":
                default:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--remote-allow-origins=*");
                    chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                    chromeOptions.addArguments("--incognito");
                    if (isCI()) {
                        chromeOptions.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
                        chromeOptions.addArguments("--disable-gpu");
                        chromeOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    }
                    chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
                    chromeOptions.setExperimentalOption("useAutomationExtension", false);
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
