package stepDefs;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ConfigReader;
import utils.LoggerUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static io.restassured.RestAssured.given;

public class API_StepDefs {
    private String token;
    private Response lastResponse;

    @Given("user gets a token from mock server")
    public void user_gets_a_token_from_mock_server() {
        LoggerUtils.info("Mock server'dan token aliniyor...");
        RestAssured.baseURI = ConfigReader.getProperty("mock.server.url");
        Response response = given()
            .header("user", "testUser")
            .header("pass", "testPass")
            .post("/token");
        token = response.jsonPath().getString("token");
        LoggerUtils.info("API Response (Token): " + response.getBody().asString());
        System.out.println("--- API Response (Token) ---\n" + response.getBody().asString());
    }

    @When("user fetches invoice with barcode {string}")
    public void user_fetches_invoice_with_barcode(String barcode) {
        LoggerUtils.info("Fatura cekiliyor, barkod: " + barcode);
        lastResponse = given()
            .queryParam("barcode", barcode)
            .get("/viewInvoice");
    }

    @Then("the invoice response should be saved to file")
    public void the_invoice_response_should_be_saved_to_file() throws IOException {
        File dir = new File("target");
        if(!dir.exists()) dir.mkdir();
        String bodyStr = lastResponse.getBody().asString();
        LoggerUtils.info("API Response (View Invoice): " + bodyStr);
        System.out.println("--- API Response (View Invoice) ---\n" + bodyStr);
        FileWriter writer = new FileWriter("target/viewInvoice_response.json");
        writer.write(bodyStr);
        writer.close();
    }

    @When("user sends invoice with barcode {string}")
    public void user_sends_invoice_with_barcode(String barcode) {
        LoggerUtils.info("Fatura gonderiliyor, barkod: " + barcode);
        lastResponse = given()
            .header("token", token)
            .header("Content-Type", "application/json")
            .body("{\"Barcode\": {\"barcode\": \"" + barcode + "\"}}")
            .post("/sendInvoice");
    }
    
    @Then("the send invoice response should be saved to file")
    public void the_send_invoice_response_should_be_saved_to_file() throws IOException {
        String bodyStr = lastResponse.getBody().asString();
        LoggerUtils.info("API Response (Send Invoice): " + bodyStr);
        System.out.println("--- API Response (Send Invoice) ---\n" + bodyStr);
        FileWriter writer = new FileWriter("target/sendInvoice_response.json");
        writer.write(bodyStr);
        writer.close();
    }
}
