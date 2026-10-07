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

    @Given("user gets a token from mock server")
    public void user_gets_a_token_from_mock_server() {
        RestAssured.baseURI = ConfigReader.getProperty("mock.server.url");
        Response response = given()
            .header("user", "testUser")
            .header("pass", "testPass")
            .post("/token");
        token = response.jsonPath().getString("token");
        // Assert token is not empty
        Assert.assertNotNull(token, "Token should not be empty");
        Assert.assertFalse(token.isEmpty(), "Token should not be empty");
    }

    @When("user fetches invoice with barcode {string}")
    public void user_fetches_invoice_with_barcode(String barcode) {
        lastResponse = given()
            .queryParam("barcode", barcode)
            .get("/viewInvoice");
        // Assert response successful
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "viewInvoice should return 200");
    }

    @Then("the invoice response should be saved to file only if successful")
    public void the_invoice_response_should_be_saved_to_file() throws IOException {
        // Assert response was successful before saving
        Assert.assertNotNull(lastResponse, "Last response should not be null");
        Assert.assertTrue(lastResponse.getStatusCode() == 200, "viewInvoice must return 200 to save response");
        Assert.assertNotNull(lastResponse.getBody(), "Response body should not be null");

        // Verify response structure
        String invoiceLink = lastResponse.jsonPath().getString("InvoiceLink");
        boolean resultSuccess = lastResponse.jsonPath().getBoolean("Result.success");
        Assert.assertNotNull(invoiceLink, "InvoiceLink should not be null");
        Assert.assertTrue(resultSuccess, "Result.success should be true");

        File dir = new File("target");
        if(!dir.exists()) dir.mkdir();
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
        // Assert response code
        Assert.assertNotNull(lastResponse, "Last response should not be null");
    }

    @Then("the send invoice response should be saved to file only if successful")
    public void the_send_invoice_response_should_be_saved_to_file() throws IOException {
        // Assert response was successful before saving
        Assert.assertNotNull(lastResponse, "Last response should not be null");
        Assert.assertTrue(lastResponse.getStatusCode() == 200, "sendInvoice should return 200");
        Assert.assertNotNull(lastResponse.getBody(), "Response body should not be null");

        // Verify response structure
        boolean success = lastResponse.jsonPath().getBoolean("success");
        String receivedBarcode = lastResponse.jsonPath().getString("receivedBarcode");
        Assert.assertTrue(success, "sendInvoice success should be true");
        Assert.assertNotNull(receivedBarcode, "receivedBarcode should not be null");

        FileWriter writer = new FileWriter("target/sendInvoice_response.json");
        writer.write(lastResponse.getBody().asString());
        writer.close();
    }
}
