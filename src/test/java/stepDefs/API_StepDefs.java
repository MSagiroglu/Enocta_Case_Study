package stepDefs;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ConfigReader;
import static utils.LoggerUtils.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static io.restassured.RestAssured.given;

public class API_StepDefs {
    private String token;
    private Response lastResponse;

    @Given("user gets a token from mock server")
    public void kullanici_mock_sunucusundan_token_alir() {
        info("Mock server'dan token aliniyor...");
        // RestAssured ayari: Temel URL'i (BaseURI) ConfigReader uzerinden properties dosyasindan cekiyoruz.
        RestAssured.baseURI = ConfigReader.getProperty("mock.server.url");
        
        // given(): Istek (request) hazirlik asamasidir. Header (baslik) bilgilerini ekliyoruz.
        // post(): Belirtilen endpoint'e POST istegi atar.
        Response response = given()
            .header("user", "testUser")
            .header("pass", "testPass")
            .post("/token");
            
        // jsonPath(): Gelen JSON yanitini (response) parcalamak (parse) icin kullanilir.
        token = response.jsonPath().getString("token");
        info("API Response (Token): " + response.getBody().asString());
    }

    @When("user fetches invoice with barcode {string}")
    public void kullanici_barkod_ile_faturayi_ceker(String barcode) {
        info("Fatura cekiliyor, barkod: " + barcode);
        
        // queryParam(): URL'in sonuna soru isareti ile eklenen (örn: ?barcode=123) parametrelerdir.
        // get(): Belirtilen endpoint'e GET istegi atar.
        lastResponse = given()
            .queryParam("barcode", barcode)
            .get("/viewInvoice");
    }

    @Then("the invoice response should be saved to file")
    public void fatura_yaniti_dosyaya_kaydedilmelidir() throws IOException {
        // Hedef (target) klasorunun varligini kontrol edip yoksa olusturuyoruz.
        File dir = new File("target");
        if(!dir.exists()) dir.mkdir();
        
        // API'den gelen yanitin tam (raw) govdesini String olarak aliyoruz.
        String bodyStr = lastResponse.getBody().asString();
        info("API Response (View Invoice): " + bodyStr);
        
        // Yaniti bir JSON dosyasi olarak target klasorune kaydediyoruz.
        FileWriter writer = new FileWriter("target/viewInvoice_response.json");
        writer.write(bodyStr);
        writer.close();
    }

    @When("user sends invoice with barcode {string}")
    public void kullanici_barkod_ile_fatura_gonderir(String barcode) {
        info("Fatura gonderiliyor, barkod: " + barcode);
        
        // header(): Yetkilendirme (Authorization) icin ilk adimda aldigimiz token'i gonderiyoruz.
        // body(): POST edilecek JSON verisini (Payload) String formatinda iletiyoruz.
        lastResponse = given()
            .header("token", token)
            .header("Content-Type", "application/json")
            .body("{\"Barcode\": {\"barcode\": \"" + barcode + "\"}}")
            .post("/sendInvoice");
    }
    
    @Then("the send invoice response should be saved to file")
    public void gonderilen_fatura_yaniti_dosyaya_kaydedilmelidir() throws IOException {
        String bodyStr = lastResponse.getBody().asString();
        info("API Response (Send Invoice): " + bodyStr);
        
        FileWriter writer = new FileWriter("target/sendInvoice_response.json");
        writer.write(bodyStr);
        writer.close();
    }
}
