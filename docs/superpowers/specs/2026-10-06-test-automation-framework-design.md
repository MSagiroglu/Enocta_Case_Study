# Test Automation Framework Design Spec

## 1. Overview
This project is an automated testing solution encompassing both UI (E-Commerce) and API (Mock Server) automation. It implements a robust, industry-standard stack utilizing Java, Selenium WebDriver, Cucumber (BDD), RestAssured, and TestNG. The entire infrastructure is orchestratable via Docker Compose to fulfill the "Bonus" requirements of the case study.

## 2. Architecture & Components

### 2.1 Technology Stack
- **Language**: Java 17
- **Build Tool**: Maven
- **UI Testing**: Selenium WebDriver (v4.x)
- **API Testing**: RestAssured
- **BDD Framework**: Cucumber (JUnit/TestNG runner)
- **Assertions**: TestNG Assertions
- **Reporting**: Allure Reports / Cucumber HTML Reports
- **Containerization**: Docker & Docker Compose

### 2.2 Docker Compose Architecture (The "Bonus" Strategy)
The environment will be spun up using a `docker-compose.yml` file comprising three distinct services:
1.  **`mock-server`**: A lightweight Node.js (Express) container hosting the 3 required endpoints (`/token`, `/viewInvoice`, `/sendInvoice`).
2.  **`selenium-hub` / `chrome-node`**: Selenium Grid containers to allow parallel execution of UI tests in isolated, headless browsers.
3.  **`test-runner`**: A Maven/Java container that executes the TestNG suite against the Selenium Hub and Mock Server.

### 2.3 Framework Design Patterns
- **Page Object Model (POM)**: UI elements and their interactions will be encapsulated within page classes (e.g., `HomePage`, `SearchPage`, `ProductDetailsPage`).
- **Singleton Driver**: A `Driver` utility class will ensure thread-safe, parallel-capable WebDriver instances.
- **Data-Driven Configuration**: A `configuration.properties` file will manage environment variables, target URLs, and the active browser (supporting cross-browser capabilities like Chrome, Firefox).

## 3. Implementation Details

### 3.1 UI Automation (n11.com)
1.  **Login**: Handle login flow (if Captcha intervenes, we will mock the session or use a non-captcha site, but n11 usually allows standard automation logins).
2.  **Search & Filter**: Search for "cep telefonu", apply the 15000-20000 TL filter.
3.  **Complex Selection**: Logic to locate the last row of products on the first page, pick a random product, navigate to details.
4.  **Seller Selection**: Iterate through available sellers on the product page, parse their ratings, and add the product from the seller with the lowest rating.
5.  **Verification**: Navigate to the cart and assert the correct product is present.
6.  **Failure Screenshots**: A Cucumber `@After` hook will capture screenshots via `TakesScreenshot` and attach them to the scenario report upon failure.

### 3.2 API Automation (Mock Server)
The Mock Server will run on port `3000` (e.g., `http://mock-server:3000`).
-   **`/token` (POST)**: Validates `user` and `pass` headers, returns `{ "token": "dummy_token" }`.
-   **`/viewInvoice` (GET)**: Accepts `barcode` query param, returns `{ "InvoiceLink": "...", "Result": {"success": true} }`.
-   **`/sendInvoice` (POST)**: Validates `token` header and `{ "Barcode": { "barcode": "..." } }` body.
RestAssured tests will hit these endpoints, assert 200 OK statuses, and use Java `FileWriter` to output the response bodies to a local `target/api_responses/` directory.

### 3.3 Parallel Execution
TestNG's `parallel="methods"` or `parallel="scenarios"` configuration in `testng.xml`, combined with `ThreadLocal<WebDriver>` in the `Driver` class, will enable parallel execution of UI and API scenarios, fulfilling another bonus requirement.

## 4. Test Data & Environments
-   Browser selection will be parameterized via Maven command line: `mvn clean verify -Dbrowser=chrome` or via `configuration.properties`.
-   Reports will be generated automatically in the `target` directory.

## 5. Scope & Ambiguity Resolution
-   *Ambiguity*: Which e-commerce site? *Resolution*: n11.com will be used.
-   *Ambiguity*: How to handle the mock server? *Resolution*: An Express.js mock server in a Docker container to demonstrate full-stack containerization.
-   *Constraint*: Must be readable, DRY, and well-named. *Resolution*: Standard Java naming conventions, reusable utility classes (`BrowserUtils`, `ApiUtils`).
