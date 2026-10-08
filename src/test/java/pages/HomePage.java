package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class HomePage extends BasePage {
    // Amazon locators
    @FindBy(id = "twotabsearchtextbox")
    public WebElement searchBox;

    @FindBy(id = "nav-search-submit-button")
    public WebElement searchButton;
    
    @FindBy(id = "sp-cc-accept")
    public WebElement cookieAccept;

    @FindBy(xpath = "//button[contains(text(), 'Alışverişe Devam Et') or contains(@alt, 'Alışverişe Devam Et')]")
    public List<WebElement> continueShoppingBtns;

    @FindBy(id = "nav-cart-count")
    public WebElement cartCount;
}
