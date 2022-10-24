package tests;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.PlaywrightFactory;
import pages.HomePage;
import pages.ShoppingCartPage;

/**
 * TestNG Test class - Checkout cart
 */
public class TS_03_VerifyCheckoutCartTests extends TestBase {

	private ShoppingCartPage shoppingCartPage;
	private List<String> productList = new ArrayList<>();

	/**
	 * BeforeClass Method to create ExtentTest in Extent Report
	 */
	@BeforeClass
	public void setupBeforeClass() {
		extentTest = reporter.createTest("TS_03 Verify Cart and Checkout the Cart", "Verify Add Products to Cart");
	}

	/**
	 * Test the cart checkout functionality without login
	 */
	@Test(priority = 1)
	public void checkoutCartWithoutLoginTest() {
		testNode = extentTest.createNode("TC_01 Verify Cart Checkout Without Login");
		testNode.assignCategory("TS_03_Open-Cart-Checkout-Cart");
		homePage = new HomePage(page, testNode);
		softAssert.assertEquals(homePage.getHomePageTitle(), HOME_PAGE_TITLE);
		Assert.assertTrue(homePage.searchProduct("Macbook"));
		String product = homePage.addProductToCart();
		Assert.assertNotNull(product);
		productList.add(product);
		shoppingCartPage = homePage.navigateToShoppingCartPage();
		Assert.assertTrue(shoppingCartPage.checkProductInCart(productList));
		Assert.assertTrue(shoppingCartPage.checkoutCart(false));
		productList.remove(product);
	}

	/**
	 * Test the cart checkout functionality with login and save the session state
	 */
	@Test(priority = 2)
	public void checkoutCartWithLoginTest() {
		testNode = extentTest.createNode("TC_01 Verify Checkout with With Login");
		testNode.assignCategory("TS_03_Open-Cart-Checkout-Cart");
		homePage = new HomePage(page, testNode);
		softAssert.assertEquals(homePage.getHomePageTitle(), HOME_PAGE_TITLE);
		loginPage = homePage.navigateToLoginPage();
		Assert.assertTrue(loginPage.doLogin(testProperties.getProperty("username"),
				testProperties.getProperty("password")));
		PlaywrightFactory.saveSessionState(page, testProperties.getProperty("sessionState"));
		testProperties.setProperty("useSessionState", "true");
		Assert.assertTrue(homePage.searchProduct("Macbook"));
		String product = homePage.addProductToCart();
		productList.add(product);
		Assert.assertNotNull(product);
		shoppingCartPage = homePage.navigateToShoppingCartPage();
		Assert.assertTrue(shoppingCartPage.checkProductInCart(productList));
		Assert.assertTrue(shoppingCartPage.checkoutCart(true));
	}

	/**
	 * Test the cart checkout functionality with adding more product and using login
	 * state saved in previous Test
	 */
	@Test(priority = 3, dependsOnMethods = { "checkoutCartWithLoginTest" })
	public void addMoreProductToCartAndCheckoutTest() {
		testNode = extentTest.createNode("TC_01 Verify Add More Product to cart and checkout");
		testNode.assignCategory("TS_03_Open-Cart-Checkout-Cart");
		homePage = new HomePage(page, testNode);
		softAssert.assertEquals(homePage.getHomePageTitle(), HOME_PAGE_TITLE);
		Assert.assertTrue(homePage.searchProduct("Samsung"));
		String product = homePage.addProductToCart();
		productList.add(product);
		Assert.assertNotNull(product);
		shoppingCartPage = homePage.navigateToShoppingCartPage();
		Assert.assertTrue(shoppingCartPage.checkProductInCart(productList));
		Assert.assertTrue(shoppingCartPage.checkoutCart(true));
		testProperties.setProperty("useSessionState", "false");
	}
}
