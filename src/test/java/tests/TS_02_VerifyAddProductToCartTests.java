package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pages.HomePage;

/**
 * TestNG Test class - Add Product to Cart
 */
public class TS_02_VerifyAddProductToCartTests extends TestBase {

	/**
	 * BeforeClass Method to create ExtentTest in Extent Report
	 */
	@BeforeClass
	public void setupBeforeClass() {
		extentTest = reporter.createTest("TS_02 Verify Add Product to Cart", "Verify Add Products to Cart");
	}

	/**
	 * Test the product search which not exists in inventory
	 */
	@Test
	public void searchProductWhichNotExistsTest() {
		testNode = extentTest.createNode("TC_01 Verify Product search which not exists",
				"Test Case 02 Expected: No results should be displayed for the invalid product search");
		testNode.assignCategory("TS_02_Open-Cart-Add-Product-to-Cart");
		homePage = new HomePage(page, testNode);
		Assert.assertFalse(homePage.searchProduct("InvalidProduct"));
	}

	/**
	 * Test the product search and add the product the Inventory without login
	 */
	@Test
	public void searchAndAddProductToCartWithoutLoginTest() {
		testNode = extentTest.createNode("TC_02 Verify Search And Add Product to Cart Without Login");
		testNode.assignCategory("TS_02_Open-Cart-Add-Product-to-Cart");
		homePage = new HomePage(page, testNode);
		Assert.assertTrue(homePage.searchProduct("Macbook"));
		Assert.assertNotNull(homePage.addProductToCart());
	}

	/**
	 * Test the product search and add the product the Inventory with login
	 */
	@Test
	public void searchAndAddProductToCartWithLoginTest() {
		testNode = extentTest.createNode("TC_03 Verify Search And Add Product to Cart With Login");
		testNode.assignCategory("TS_02_Open-Cart-Add-Product-to-Cart");
		homePage = new HomePage(page, testNode);
		loginPage = homePage.navigateToLoginPage();
		Assert.assertTrue(loginPage.doLogin(testProperties.getProperty("username"),
				testProperties.getProperty("password")));
		Assert.assertTrue(homePage.searchProduct("Macbook"));
		Assert.assertNotNull(homePage.addProductToCart());
	}

}
