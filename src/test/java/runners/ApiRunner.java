package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeTest;
import utils.Driver;

@CucumberOptions(
    tags = "@api",
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"json:target/cucumber-api.json", "html:target/cucumber-api.html", "rerun:target/failed_scenarios_api.txt"}
)
public class ApiRunner extends AbstractTestNGCucumberTests {
    static { MockServerManager.startServer(); }
}
