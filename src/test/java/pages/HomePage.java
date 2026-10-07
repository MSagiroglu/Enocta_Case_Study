package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {
    // Amazon locators
    @FindBy(id = "twotabsearchtextbox")
    public WebElement searchBox;

    @FindBy(id = "nav-search-submit-button")
    public WebElement searchButton;
    
    @FindBy(id = "sp-cc-accept")
    public WebElement cookieAccept;
}
