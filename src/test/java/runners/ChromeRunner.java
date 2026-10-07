package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeTest;
import utils.Driver;

@CucumberOptions(
    tags = "@ui",
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"json:target/cucumber-chrome.json", "html:target/cucumber-chrome.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm", "rerun:target/failed_scenarios_chrome.txt"}
)
public class ChromeRunner extends AbstractTestNGCucumberTests {
    static { MockServerManager.startServer(); }

    @BeforeTest
    public void setupBrowser() {
        Driver.setBrowser("chrome");
    }
}
