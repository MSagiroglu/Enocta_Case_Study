package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.Driver;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"pretty", "html:target/cucumber-reports.html"}
)
public class TestRunner extends AbstractTestNGCucumberTests {
    
    @BeforeTest
    @Parameters("browser")
    public void setupBrowser(@Optional String browser) {
        if (browser != null) {
            Driver.setBrowser(browser);
        }
    }
    
    private static Process nodeProcess;
    
    static {
        try {
            // Check if node is installed and start mock server automatically
            ProcessBuilder pb = new ProcessBuilder("node", "mock-server/server.js");
            pb.redirectErrorStream(true);
            nodeProcess = pb.start();
            System.out.println("===> Started Mock Server programmatically! <===");
            
            // Ensure process is killed when tests finish
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(nodeProcess != null) {
                    nodeProcess.destroy();
                    System.out.println("===> Stopped Mock Server <===");
                }
            }));
            
            // Give node.js a moment to bind to port 3000
            Thread.sleep(3000);
        } catch(Exception e) {
            System.err.println("Could not start mock server. Ensure Node.js is installed. Error: " + e.getMessage());
        }
    }

    @Override
    @DataProvider
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
