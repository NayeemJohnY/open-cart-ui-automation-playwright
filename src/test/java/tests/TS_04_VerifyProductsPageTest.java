package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pages.HomePage;
import pages.ProductPage;
import testutils.RetryAnalyzer;

/**
 * TestNG Test class - View Products
 */
public class TS_04_VerifyProductsPageTest extends TestBase {

    /**
     * BeforeClass Method to create ExtentTest in Extent Report
     */
    @BeforeClass
    public void setupBeforeClass() {
        extentTest = reporter.createTest("TS_04 Verify Products Page",
                "Verify Products page functionalities like view products, sort products, share product");
    }

    /**
     * Test the sort products by price (Low to High) feature in all products page
     */
    @Test
    public void sortProductsByPriceTest() {
        testNode = extentTest.createNode("TC_01 Verify Product Sort By Price");
        testNode.assignCategory("TS_04_Open-Cart-Product-Page");
        HomePage homePage = new HomePage(page, testNode);
        ProductPage productPage = homePage.navigateToProductsFromMenu("Desktops");
        Assert.assertTrue(productPage.checkProductSortedByPrice());
    }

    /**
     * Test the view and share product to social platforms
     * 
     * @param socialPlatformName - {@link String} - social platform name from the
     *                           data provider
     */
    @Test(dataProvider = "getProductAndSocialPlatform", retryAnalyzer = RetryAnalyzer.class)
    public void shareProductToSocialPlatformTest(String productName, String socialPlatformName) {
        String testNodeName = "TC_01 Verify Share '" + productName  + "' Product to Social Platform - " + socialPlatformName;
        Object retryCountAttribute = Reporter.getCurrentTestResult().getTestContext().removeAttribute("retryCount");
        int retryAttempt = 0;
        if (retryCountAttribute != null){
            retryAttempt = (int)retryCountAttribute;
            testNodeName = "Retry " + retryAttempt + " : " + testNodeName;
        }
        testNode = extentTest.createNode(testNodeName);
        testNode.assignCategory("TS_04_Open-Cart-Product-Page");

        HomePage homePage = new HomePage(page, testNode);
        ProductPage productPage = homePage.navigateToProductsFromMenu("Desktops");
        productPage.viewProduct(productName);
        Assert.assertTrue(productPage.shareProductToSocialPlatform(socialPlatformName, retryAttempt));
    }

    /**
     * Data provider method for the test
     * 
     * @return Returns social platform names Object array
     */
    @DataProvider(name = "getProductAndSocialPlatform")
    public Object[][] getProductAndSocialPlatform() {
        return new Object[][] {
                { "MacBook Air", "Facebook" },
                { "Canon EOS 5D", "Twitter" },
                { "MacBook Air", "Link" },
                { "Apple Cinema 30\"", "LinkedIn" },
                { "Product 8", "Email" },
                { "Sony VAIO", "Tumblr" },
                { "HP LP3065", "NoPlatform" },
                { "MacBook", "Pinterest" }
        };
    }
}
