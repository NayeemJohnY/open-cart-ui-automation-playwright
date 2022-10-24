package pages;

import static base.PlaywrightFactory.takeScreenshot;
import static utils.ExtentReporter.extentLog;
import static utils.ExtentReporter.extentLogWithScreenshot;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object Class for Home Page
 * 
 * @author Nayeem John
 */
public class HomePage {

	private Page page;
	private ExtentTest extentTest;

	private String search = "input[name='search']";
	private String searchIcon = "div#search button";
	private String searchPageHeader = "div#content h1";
	private String loginLink = "a:text('Login')";
	private String myAccountLink = "a[title='My Account']";
	private String productSearchResult = "div.product-thumb";
	private String addToCartSelector = "text='Add to Cart'";
	private String alertSelector = "div.alert";
	private String productCaption = ".caption h4 a";
	private String shoppingCartLink = "//a[contains(text(), 'shopping cart')]";
	private String shoppingCartIcon = "div#cart";
	private String viewCartLink = "text='View Cart'";
	private String menuBarId = "#menu";
	private String seeAllClass = ".see-all";
	private String productHeaderCss = "#content h2";

	/**
	 * Constructor to initialize the page objects with the {@link Page} instance and
	 * {@link ExtentTest} instance
	 * 
	 * @param page       - {@link Page}
	 * @param extentTest - {@link ExtentTest}
	 */
	public HomePage(Page page, ExtentTest extentTest) {
		this.page = page;
		this.extentTest = extentTest;
	}

	/**
	 * Method to retrieve the Home Page title
	 * 
	 * @return String - Returns page title
	 */
	public String getHomePageTitle() {
		page.waitForLoadState();
		return page.title();
	}

	/**
	 * Method to search item in the portal for productName
	 * 
	 * @param productName - Name of the product to search
	 * @return boolean - Returns true if search found results else false
	 */
	public boolean searchProduct(String productName) {
		page.fill(search, productName);
		page.click(searchIcon);
		String header = page.textContent(searchPageHeader);
		extentLog(extentTest, Status.PASS, "Search of '" + header + "' Product is successful");
		if (page.locator(productSearchResult).count() > 0) {
			extentLog(extentTest, Status.PASS, "Search of '" + productName + "' Product is successful");
			return true;
		}
		extentLogWithScreenshot(extentTest, Status.FAIL, "No Product is available for the search '" + productName + "'",
				takeScreenshot(page));
		return false;
	}

	/**
	 * Method to add a product to the cart. Product will be added to cart and
	 * screenshot is taken
	 * 
	 * @return String - Returns actual product catalog name
	 */
	public String addProductToCart() {
		Locator productLocator = page.locator(productSearchResult).nth(0);
		productLocator.locator(addToCartSelector).click();
		String product = productLocator.locator(productCaption).textContent();
		if (page.textContent(alertSelector).contains("You have added " + product + " to your shopping cart!")) {
			extentLogWithScreenshot(extentTest, Status.PASS, "The '" + product + "' product is added to the cart.",
					takeScreenshot(page));
			return product;
		}
		extentLog(extentTest, Status.FAIL, "Unable to add the product to the cart");
		return null;
	}

	/**
	 * Method to navigate from Homepage to Login page
	 * 
	 * @return LoginPage - Returns {@link LoginPage} instance
	 */
	public LoginPage navigateToLoginPage() {
		page.click(myAccountLink);
		page.click(loginLink);
		return new LoginPage(page, extentTest);
	}

	/**
	 * Method to navigate from Homepage to Shopping cart page
	 * 
	 * @return ShoppingCartPage - Returns {@link ShoppingCartPage} instance
	 */
	public ShoppingCartPage navigateToShoppingCartPage() {
		if (page.isVisible(shoppingCartLink)) {
			page.click(shoppingCartLink);
		} else {
			page.click(shoppingCartIcon);
			page.click(viewCartLink);
		}
		return new ShoppingCartPage(page, extentTest);
	}

	/**
	 * Method to navigate to specific products menu from Homepage
	 * 
	 * @param productOption - {@link String}
	 * @return ProductPage - Returns {@link ProductPage} instance
	 */
	public ProductPage navigateToProductsFromMenu(String productOption) {
		Locator productMenu = page.locator(menuBarId).locator("li",
				new Locator.LocatorOptions().setHas(page.locator("text='" + productOption + "'")));
		productMenu.hover();
		productMenu.locator(seeAllClass).click();
		String productTitle = page.textContent(productHeaderCss);
		if (productTitle.equals(productOption)) {
			extentLog(extentTest, Status.PASS, "Navigated to product page from navigation menu");
			return new ProductPage(page, extentTest);
		} else {
			String message = "Incorrect Landing Page - <br/>Expected product page: " + productOption
					+ " Actual product page: " + productTitle;
			extentLogWithScreenshot(extentTest, Status.FAIL, message, takeScreenshot(page));
			throw new IllegalStateException(message);
		}

	}
}
