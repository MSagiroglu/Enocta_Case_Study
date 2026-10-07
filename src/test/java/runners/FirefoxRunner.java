package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeTest;
import utils.Driver;

@CucumberOptions(
    tags = "@all",
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"json:target/cucumber-firefox.json", "html:target/cucumber-firefox.html", "rerun:target/failed_scenarios_firefox.txt"}
)
public class FirefoxRunner extends AbstractTestNGCucumberTests {
    static { MockServerManager.startServer(); }

    @BeforeTest
    public void setupBrowser() {
        Driver.setBrowser("firefox");
    }
}
