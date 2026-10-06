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
