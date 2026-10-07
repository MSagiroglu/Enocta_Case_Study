package stepDefs;

import pages.CartPage;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchPage;

public class BaseStep {
    // Tum Step Definition siniflarinin (extends edenlerin) ulasabilecegi Page nesneleri.
    // Lazy Initialization veya doğrudan burada tanimlanabilir.
    
    protected HomePage homePage = new HomePage();
    protected SearchPage searchPage = new SearchPage();
    protected ProductPage productPage = new ProductPage();
    protected CartPage cartPage = new CartPage();
}
