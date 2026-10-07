package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.Driver;

@CucumberOptions(
    glue = {"stepDefs", "hooks"}
)
public class FailedTestRunner extends AbstractTestNGCucumberTests {
    
    @BeforeTest
    @Parameters("browser")
    public void setupBrowser(@Optional String browser) {
        String browserName = browser != null ? browser : "default";
        String rerunFile = "target/failed_scenarios_" + browserName + ".txt";
        
        // Eger o browser'da hata yoksa ve dosya olusmamissa, Cucumber patlamasin diye bos bir dosya olustur
        try {
            java.io.File file = new java.io.File(rerunFile);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (Exception e) {}

        if (browser != null) {
            Driver.setBrowser(browser);
        }
        
        System.setProperty("cucumber.features", "@" + rerunFile);
        System.setProperty("cucumber.plugin", "json:target/cucumber-failed-" + browserName + ".json, html:target/cucumber-failed-" + browserName + ".html");
    }

    @Override
    @DataProvider(parallel = false) // Failed testler guvenlik amaciyla genelde sirayla kosulur
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
