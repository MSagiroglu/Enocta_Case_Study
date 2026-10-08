package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class SearchPage extends BasePage {
    @FindBy(id = "low-price")
    public WebElement minPriceInput;

    @FindBy(id = "high-price")
    public WebElement maxPriceInput;

    @FindBy(className = "a-button-input")
    public WebElement goButton;

    // Use a robust selector for search results that excludes sponsored carousels at the very bottom
    @FindBy(css = "div.s-main-slot div[data-component-type='s-search-result']")
    public List<WebElement> productList;
    
    @FindBy(xpath = "//span[text()='Düşükten Yükseğe']")
    public WebElement sortLowToHighOption;
    
    @FindBy(className = "a-dropdown-prompt")
    public WebElement sortDropdown;

    @FindBy(css = "div.s-main-slot")
    public WebElement mainSlot;

    // Helper method to keep By locators inside Page class and avoid strict FindBy limitations for relative locators
    public String getProductUrl(WebElement product) {
        return product.findElement(org.openqa.selenium.By.xpath(".//a[contains(@href, '/dp/')]")).getAttribute("href");
    }
}
