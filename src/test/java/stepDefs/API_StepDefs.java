package stepDefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import utils.ConfigReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static io.restassured.RestAssured.given;

public class API_StepDefs {
    private String token;
    private Response lastResponse;

    @Given("kullanici mock server'dan token alir")
    public void kullanici_mock_serverdan_token_alir() {
        RestAssured.baseURI = ConfigReader.getProperty("mock.server.url");
        Response response = given()
            .header("user", "testUser")
            .header("pass", "testPass")
            .post("/token");
        token = response.jsonPath().getString("token");
        // Token'in bos olmadigini dogrula
        Assert.assertNotNull(token, "Token bos olmamali");
        Assert.assertFalse(token.isEmpty(), "Token bos olmamali");
    }

    @When("kullanici {string} barkodlu faturayi sorgular")
    public void kullanici_barkodlu_faturayi_sorgular(String barcode) {
        lastResponse = given()
            .queryParam("barcode", barcode)
            .get("/viewInvoice");
        // Response'un basarili oldugunu (200) dogrula
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "viewInvoice 200 donmeli");
    }

    @Then("basarili fatura sorgusu yaniti dosyaya kaydedilir")
    public void basarili_fatura_sorgusu_yaniti_dosyaya_kaydedilir() throws IOException {
        // Dosyaya yazmadan once response'un basarili oldugunu dogrula
        Assert.assertNotNull(lastResponse, "Son response null olmamali");
        Assert.assertTrue(lastResponse.getStatusCode() == 200, "Response kaydetmek icin viewInvoice 200 donmeli");
        Assert.assertNotNull(lastResponse.getBody(), "Response body null olmamali");

        // Response yapisini dogrula
        String invoiceLink = lastResponse.jsonPath().getString("InvoiceLink");
        boolean resultSuccess = lastResponse.jsonPath().getBoolean("Result.success");
        Assert.assertNotNull(invoiceLink, "InvoiceLink null olmamali");
        Assert.assertTrue(resultSuccess, "Result.success true olmali");

        File dir = new File("target");
        if(!dir.exists()) dir.mkdir();
        FileWriter writer = new FileWriter("target/viewInvoice_response.json");
        writer.write(lastResponse.getBody().asString());
        writer.close();
    }

    @When("kullanici {string} barkodlu faturayi gonderir")
    public void kullanici_barkodlu_faturayi_gonderir(String barcode) {
        lastResponse = given()
            .header("token", token)
            .header("Content-Type", "application/json")
            .body("{\"Barcode\": {\"barcode\": \"" + barcode + "\"}}")
            .post("/sendInvoice");
        // Response'un bos donmedigini dogrula
        Assert.assertNotNull(lastResponse, "Son response null olmamali");
    }

    @Then("basarili fatura gonderme yaniti dosyaya kaydedilir")
    public void basarili_fatura_gonderme_yaniti_dosyaya_kaydedilir() throws IOException {
        // Dosyaya yazmadan once response'un basarili oldugunu dogrula
        Assert.assertNotNull(lastResponse, "Son response null olmamali");
        Assert.assertTrue(lastResponse.getStatusCode() == 200, "sendInvoice 200 donmeli");
        Assert.assertNotNull(lastResponse.getBody(), "Response body null olmamali");

        // Response yapisini dogrula
        boolean success = lastResponse.jsonPath().getBoolean("success");
        String receivedBarcode = lastResponse.jsonPath().getString("receivedBarcode");
        Assert.assertTrue(success, "sendInvoice success true olmali");
        Assert.assertNotNull(receivedBarcode, "receivedBarcode null olmamali");

        FileWriter writer = new FileWriter("target/sendInvoice_response.json");
        writer.write(lastResponse.getBody().asString());
        writer.close();
    }
}
