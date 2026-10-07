package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class CartPage extends BasePage {
    @FindBy(css = ".sc-list-item")
    public List<WebElement> cartItems;

    @FindBy(css = ".sc-product-title")
    public List<WebElement> cartItemTitles;
    
    @FindBy(css = ".sc-product-price")
    public List<WebElement> cartItemPrices;
}
