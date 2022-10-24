package pages;

import static base.PlaywrightFactory.takeScreenshot;
import static utils.ExtentReporter.extentLogWithScreenshot;

import java.util.List;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;

/**
 * Page Object Class for shopping cart Page
 * 
 * @author Nayeem John
 */
public class ShoppingCartPage {

    private Page page;
    private ExtentTest extentTest;

    private String checkout = "//a[text()='Checkout']";
    private String login = "//input[@value='Login']";
    private String billingAddress = "//a[@href='#collapse-payment-address']";

    /**
     * Constructor to initialize the page objects with the {@link Page} instance and
     * {@link ExtentTest} instance
     * 
     * @param page       - {@link Page}
     * @param extentTest - {@link ExtentTest}
     */
    public ShoppingCartPage(Page page, ExtentTest extentTest) {
        this.page = page;
        this.extentTest = extentTest;
    }

    /**
     * Method to verify the product added to cart is available in cart
     * 
     * @param products - {@link List<String>} Of products to verify
     * @return boolean - Returns true if all products is available in cart else
     *         false if any one product is not available
     */
    public boolean checkProductInCart(List<String> products) {
        for (String product : products) {
            String productInCartSelector = "//div[@id='content']//a[text()='" + product + "']";
            if (!page.locator(productInCartSelector).isVisible()) {
                extentLogWithScreenshot(extentTest, Status.FAIL,
                        "The '" + product + "' Product is not available to the cart", takeScreenshot(page));
                return false;
            }
        }
        extentLogWithScreenshot(extentTest, Status.PASS,
                "The '" + products.toString() + "' Products is available to the cart", takeScreenshot(page));
        return true;
    }

    /**
     * Method to checkout the cart with the product items
     * 
     * @param isUserLoggedIn - {@link Boolean} - User Login state
     * @return boolean - Returns true if checkout was successful else @throws {@link
     *         com.microsoft.playwright.TimeoutError}
     *         Exception
     */
    public boolean checkoutCart(boolean isUserLoggedIn) {
        page.click(checkout);
        if (isUserLoggedIn) {
            return page.waitForSelector(billingAddress).isVisible();
        } else {
            return page.waitForSelector(login).isVisible();
        }
    }
}
