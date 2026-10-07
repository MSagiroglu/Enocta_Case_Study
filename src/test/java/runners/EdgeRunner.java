package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeTest;
import utils.Driver;

@CucumberOptions(
    tags = "@all",
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"json:target/cucumber-edge.json", "html:target/cucumber-edge.html", "rerun:target/failed_scenarios_edge.txt"}
)
public class EdgeRunner extends AbstractTestNGCucumberTests {
    static { MockServerManager.startServer(); }

    @BeforeTest
    public void setupBrowser() {
        Driver.setBrowser("edge");
    }
}
