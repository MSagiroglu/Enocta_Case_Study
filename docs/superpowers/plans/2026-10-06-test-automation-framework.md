# Test Automation Framework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a robust, Dockerized test automation framework for UI and API tests using Java, Selenium, Cucumber, and RestAssured.

**Architecture:** A Maven project orchestrating Page Object Model (POM) UI tests against n11.com and API tests against a locally built Express.js mock server, entirely wrapped within a `docker-compose.yml` environment.

**Tech Stack:** Java 17, Maven, Selenium 4, Cucumber, RestAssured, TestNG, Docker.

## Global Constraints

- Must follow BDD (Behavior Driven Development) approach
- Browsers must be parametrized (e.g. Chrome, Firefox)
- Senaryo failure -> Screenshot capture
- Must run via Docker / Docker Compose (Bonus)
- Must support Parallel Execution (Bonus)
- Dry, readable, no code repetition.
- Project uploaded to VCS with README.md

---

### Task 1: Scaffolding and Docker Compose Setup

**Files:**
- Create: `pom.xml`
- Create: `docker-compose.yml`
- Create: `mock-server/package.json`
- Create: `mock-server/server.js`
- Create: `mock-server/Dockerfile`
- Create: `Dockerfile` (for Test Runner)

**Interfaces:**
- Consumes: None
- Produces: Base Maven structure, Mock API at `http://mock-server:3000`, Selenium Hub at `http://selenium-hub:4444`.

- [ ] **Step 1: Write pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.enocta</groupId>
    <artifactId>automation-case</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <cucumber.version>7.14.0</cucumber.version>
        <selenium.version>4.16.1</selenium.version>
        <restassured.version>5.4.0</restassured.version>
        <testng.version>7.8.0</testng.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>${cucumber.version}</version>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${restassured.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.16.0</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.3</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>src/test/resources/runners/testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create Mock Server application**
Write `mock-server/package.json`
```json
{
  "name": "mock-api",
  "version": "1.0.0",
  "main": "server.js",
  "dependencies": {
    "express": "^4.18.2"
  }
}
```

Write `mock-server/server.js`
```javascript
const express = require('express');
const app = express();
app.use(express.json());

app.post('/token', (req, res) => {
    if (req.headers.user && req.headers.pass) {
        res.json({ token: "sample_token_123" });
    } else {
        res.status(401).send();
    }
});

app.get('/viewInvoice', (req, res) => {
    const barcode = req.query.barcode;
    res.json({
        InvoiceLink: "http://abc.com/invoice.pdf",
        Result: { success: true }
    });
});

app.post('/sendInvoice', (req, res) => {
    if (req.headers.token === "sample_token_123" && req.body.Barcode) {
        res.json({ success: true, receivedBarcode: req.body.Barcode.barcode });
    } else {
        res.status(401).send();
    }
});

app.listen(3000, () => console.log('Mock Server running on 3000'));
```

Write `mock-server/Dockerfile`
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY server.js .
EXPOSE 3000
CMD ["node", "server.js"]
```

- [ ] **Step 3: Create Test Runner Dockerfile**
Write `Dockerfile`
```dockerfile
FROM maven:3.9.5-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
# Download dependencies for offline usage
RUN mvn dependency:go-offline
COPY src ./src
```

- [ ] **Step 4: Create docker-compose.yml**
Write `docker-compose.yml`
```yaml
version: '3.8'

services:
  mock-server:
    build: ./mock-server
    ports:
      - "3000:3000"
      
  selenium-hub:
    image: selenium/hub:4.16.1
    ports:
      - "4442-4444:4442-4444"

  chrome:
    image: selenium/node-chrome:4.16.1
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443

  test-runner:
    build: .
    depends_on:
      - mock-server
      - chrome
    environment:
      - BROWSER=remote-chrome
      - MOCK_SERVER_URL=http://mock-server:3000
      - HUB_URL=http://selenium-hub:4444/wd/hub
    command: mvn clean verify
```

- [ ] **Step 5: Run npm install locally to verify mock server (Optional)**
```bash
cd mock-server && npm install && cd ..
```

---

### Task 2: Core Test Framework Utilities

**Files:**
- Create: `src/test/java/utils/ConfigReader.java`
- Create: `src/test/java/utils/Driver.java`
- Create: `src/test/resources/configuration.properties`

**Interfaces:**
- Consumes: None
- Produces: `ConfigReader.getProperty(String key)`, `Driver.getDriver()` ThreadLocal webdriver instances.

- [ ] **Step 1: Write configuration.properties**
```properties
browser=chrome
hub.url=http://localhost:4444/wd/hub
mock.server.url=http://localhost:3000
n11.url=https://www.n11.com
```

- [ ] **Step 2: Write ConfigReader.java**
```java
package utils;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            FileInputStream file = new FileInputStream("src/test/resources/configuration.properties");
            properties = new Properties();
            properties.load(file);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String sysProp = System.getenv(key.toUpperCase().replace(".", "_"));
        if (sysProp != null) return sysProp;
        return properties.getProperty(key);
    }
}
```

- [ ] **Step 3: Write Driver.java**
```java
package utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.time.Duration;

public class Driver {
    private static ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driverPool.get() == null) {
            String browser = ConfigReader.getProperty("browser");
            switch (browser.toLowerCase()) {
                case "remote-chrome":
                    try {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                        driverPool.set(new RemoteWebDriver(new URL(ConfigReader.getProperty("hub.url")), options));
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
                case "firefox":
                    driverPool.set(new FirefoxDriver());
                    break;
                case "chrome":
                default:
                    driverPool.set(new ChromeDriver());
                    break;
            }
            driverPool.get().manage().window().maximize();
            driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
```

---

### Task 3: BDD Features and Page Objects

**Files:**
- Create: `src/test/resources/features/ECommerce.feature`
- Create: `src/test/resources/features/Api.feature`
- Create: `src/test/java/pages/BasePage.java`
- Create: `src/test/java/pages/HomePage.java`

**Interfaces:**
- Consumes: `Driver.getDriver()`
- Produces: Page actions

- [ ] **Step 1: Write ECommerce.feature**
```gherkin
Feature: E-Commerce Product Purchase
  
  Scenario: Search and Add to Cart from Lowest Rated Seller
    Given user navigates to n11
    When user searches for "cep telefonu"
    And user filters price between "15000" and "20000"
    And user selects a random product from the last row
    And user adds the product to the cart from the seller with the lowest rating
    Then user verifies the product is in the cart
```

- [ ] **Step 2: Write Api.feature**
```gherkin
Feature: API Mock Server Tests
  
  Scenario: Test Token, View Invoice and Send Invoice
    Given user gets a token from mock server
    When user fetches invoice with barcode "12345"
    Then the invoice response should be saved to file
    When user sends invoice with barcode "12345"
    Then the send invoice response should be saved to file
```

- [ ] **Step 3: Write BasePage.java and HomePage.java**
```java
package pages;
import org.openqa.selenium.support.PageFactory;
import utils.Driver;

public abstract class BasePage {
    public BasePage() {
        PageFactory.initElements(Driver.getDriver(), this);
    }
}
```

```java
package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {
    @FindBy(id = "searchData")
    public WebElement searchBox;

    @FindBy(className = "searchBtn")
    public WebElement searchButton;
}
```

---

### Task 4: Step Definitions and Hooks

**Files:**
- Create: `src/test/java/hooks/Hooks.java`
- Create: `src/test/java/stepDefs/UI_StepDefs.java`
- Create: `src/test/java/stepDefs/API_StepDefs.java`
- Create: `src/test/resources/runners/testng.xml`
- Create: `src/test/java/runners/TestRunner.java`

**Interfaces:**
- Consumes: Features, Page Objects, RestAssured
- Produces: Executable tests

- [ ] **Step 1: Write Hooks.java (Screenshot on Failure)**
```java
package hooks;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.Driver;

public class Hooks {
    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            final byte[] screenshot = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
        }
        Driver.closeDriver();
    }
}
```

- [ ] **Step 2: Write UI_StepDefs.java (Stubs for now)**
```java
package stepDefs;
import io.cucumber.java.en.*;
import pages.HomePage;
import utils.ConfigReader;
import utils.Driver;

public class UI_StepDefs {
    HomePage homePage = new HomePage();

    @Given("user navigates to n11")
    public void user_navigates_to_n11() {
        Driver.getDriver().get(ConfigReader.getProperty("n11.url"));
    }

    @When("user searches for {string}")
    public void user_searches_for(String item) {
        homePage.searchBox.sendKeys(item);
        homePage.searchButton.click();
    }

    @When("user filters price between {string} and {string}")
    public void user_filters_price_between_and(String min, String max) {
        // Find elements for min and max price, and click apply.
        // Needs specific n11 locators.
    }

    @When("user selects a random product from the last row")
    public void user_selects_a_random_product_from_the_last_row() {
       // Logic to find elements in the last row and click random
    }

    @When("user adds the product to the cart from the seller with the lowest rating")
    public void user_adds_the_product_to_the_cart_from_the_seller_with_the_lowest_rating() {
       // Logic to parse sellers list
    }

    @Then("user verifies the product is in the cart")
    public void user_verifies_the_product_is_in_the_cart() {
       // Navigate to cart and assert
    }
}
```

- [ ] **Step 3: Write API_StepDefs.java**
```java
package stepDefs;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ConfigReader;
import java.io.FileWriter;
import java.io.IOException;
import static io.restassured.RestAssured.given;

public class API_StepDefs {
    private String token;
    private Response lastResponse;

    @Given("user gets a token from mock server")
    public void user_gets_a_token_from_mock_server() {
        RestAssured.baseURI = ConfigReader.getProperty("mock.server.url");
        Response response = given()
            .header("user", "testUser")
            .header("pass", "testPass")
            .post("/token");
        token = response.jsonPath().getString("token");
    }

    @When("user fetches invoice with barcode {string}")
    public void user_fetches_invoice_with_barcode(String barcode) {
        lastResponse = given()
            .queryParam("barcode", barcode)
            .get("/viewInvoice");
    }

    @Then("the invoice response should be saved to file")
    public void the_invoice_response_should_be_saved_to_file() throws IOException {
        FileWriter writer = new FileWriter("target/viewInvoice_response.json");
        writer.write(lastResponse.getBody().asString());
        writer.close();
    }

    @When("user sends invoice with barcode {string}")
    public void user_sends_invoice_with_barcode(String barcode) {
        lastResponse = given()
            .header("token", token)
            .header("Content-Type", "application/json")
            .body("{\"Barcode\": {\"barcode\": \"" + barcode + "\"}}")
            .post("/sendInvoice");
    }
    
    @Then("the send invoice response should be saved to file")
    public void the_send_invoice_response_should_be_saved_to_file() throws IOException {
        FileWriter writer = new FileWriter("target/sendInvoice_response.json");
        writer.write(lastResponse.getBody().asString());
        writer.close();
    }
}
```

- [ ] **Step 4: Write TestRunner.java and testng.xml for parallel execution**
```java
package runners;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepDefs", "hooks"},
    plugin = {"pretty", "html:target/cucumber-reports.html"}
)
public class TestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Test Suite" parallel="methods" thread-count="2">
    <test name="Automation Tests">
        <classes>
            <class name="runners.TestRunner"/>
        </classes>
    </test>
</suite>
```

---
### Task 5: Finish UI Locators & README
- [ ] **Step 1: Complete locators logic**
(This step requires real-time DOM analysis of n11.com or mock placeholders if bot protection hits, implemented in UI_StepDefs)

- [ ] **Step 2: Create README.md**
```markdown
# Enocta Automation Case

## Tech Stack
Java 17, Selenium 4, Cucumber BDD, RestAssured, TestNG, Docker

## How to Run locally
1. `mvn clean verify`

## How to Run with Docker (Parallel UI Tests + Mock Server)
1. `docker-compose up --build`
Tests will run against headless Chrome grid.
```
