package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class CartPage extends BasePage {
    @FindBy(css = ".sc-list-item")
    public List<WebElement> cartItems;

    @FindBy(css = ".sc-product-title, .a-truncate-cut, .sc-item-product-title")
    public List<WebElement> cartItemTitles;
    
    @FindBy(css = ".sc-product-price, .sc-item-price, span.sc-price")
    public List<WebElement> cartItemPrices;
}
