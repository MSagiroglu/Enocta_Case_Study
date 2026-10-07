package pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class ProductPage extends BasePage {
    @FindBy(id = "productTitle")
    public WebElement productTitle;
    
    @FindBy(css = ".a-price.a-text-price.a-size-medium .a-offscreen, .a-price .a-offscreen")
    public List<WebElement> productPrices;

    @FindBy(xpath = "//a[contains(@title, 'diğer satıcı')] | //a[contains(text(), 'Yeni ve İkinci El')] | //a[contains(text(), 'diğer seçenek')] | //a[contains(@title, 'Daha Fazla')] | //a[contains(@href, 'offer-listing')] | //div[@id='moreBuyingChoices_feature_div']//a")
    public WebElement otherSellersLink;

    @FindBy(id = "aod-offer-list")
    public WebElement otherSellersPanel;

    @FindBy(css = "#aod-offer-list .aod-information-block")
    public List<WebElement> otherSellersList;

    @FindBy(css = "input#add-to-cart-button, input[name='submit.addToCart'], span#submit\\.add-to-cart-announce, #buybox-see-all-buying-choices")
    public WebElement defaultAddToCartButton;
}
