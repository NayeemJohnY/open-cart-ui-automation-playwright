package tests;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pages.HomePage;

/**
 * TestNG Test class - Login Functionality
 */
public class TS_01_VerifyOpenCartLoginTests extends TestBase {

	/**
	 * BeforeClass Method to create ExtentTest in Extent Report
	 */
	@BeforeClass
	public void setupBeforeClass() {
		extentTest = reporter.createTest("TS_01 Verify Open Cart Login", "Verify login functionality of Open Cart");
	}

	/**
	 * Test the login functionality of the application with valid credentials
	 * This test will soft assert the home page title and validate the login
	 */
	@Test
	public void loginWithValidCredentialsTest() {
		testNode = extentTest.createNode("TC_01 Verify Open Cart Login with Valid Credentials");
		testNode.assignCategory("TS_01_Open-Cart-Login");
		homePage = new HomePage(page, testNode);
		softAssert.assertEquals(homePage.getHomePageTitle(), HOME_PAGE_TITLE);
		loginPage = homePage.navigateToLoginPage();
		Assert.assertTrue(loginPage.doLogin(testProperties.getProperty("username"),
				testProperties.getProperty("password")));
	}

	/**
	 * Test the login functionality of the application with invalid credentials
	 * This test will soft assert the home page title and validate the login
	 */
	@Test
	public void loginWithInvalidCredentialsTest() {
		testNode = extentTest.createNode("TC_02 Verify Open Cart Login with Invalid Credentials");
		testNode.assignCategory("TS_01_Open-Cart-Login");
		homePage = new HomePage(page, testNode);
		softAssert.assertEquals(homePage.getHomePageTitle(), HOME_PAGE_TITLE);
		loginPage = homePage.navigateToLoginPage();
		Assert.assertFalse(loginPage.doLogin(UUID.randomUUID().toString().replace("-", ""), "InvalidPassword"));
	}
}
