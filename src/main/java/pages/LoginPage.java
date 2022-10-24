package pages;

import static base.PlaywrightFactory.takeScreenshot;
import static utils.ExtentReporter.extentLogWithScreenshot;

import java.util.Base64;

import static utils.ExtentReporter.extentLog;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;

/**
 * Page Object Class for Login Page
 * 
 * @author Nayeem John
 */
public class LoginPage {

    private Page page;
    private ExtentTest extentTest;
    private String emailId = "//input[@id='input-email']";
    private String password = "//input[@id='input-password']";
    private String loginBtn = "//input[@value='Login']";
    private String logoutLink = "//a[@class='list-group-item'][normalize-space()='Logout']";
    private String alertErrorSelector = "div.alert";

    /**
     * Constructor to initialize the page objects with the {@link Page} instance and
     * {@link ExtentTest} instance
     * 
     * @param page       - {@link Page}
     * @param extentTest - {@link ExtentTest}
     */
    public LoginPage(Page page, ExtentTest extentTest) {
        this.page = page;
        this.extentTest = extentTest;
    }

    /**
     * Method to get Login page title
     * 
     * @return String - Returns page title
     */
    public String getLoginPageTitle() {
        page.waitForLoadState();
        return page.title();
    }

    /**
     * Method to Login using the username and password
     * 
     * @param appUserName - {@link String} username for the App
     * @param appPassword - {@link String} username for the password
     * @return boolean - Returns true after successful login else false
     */
    public boolean doLogin(String appUserName, String appPassword) {
        extentLog(extentTest, Status.INFO, "Login to Application using username " + appUserName);
        page.fill(emailId, appUserName);
        page.fill(password, new String(Base64.getDecoder().decode(appPassword)));
        page.click(loginBtn);
        if (page.locator(logoutLink).isVisible()) {
            extentLog(extentTest, Status.PASS, "User login to the Application successful.");
            return true;
        }
        boolean isErrorDisplayed = page.textContent(alertErrorSelector)
                .contains("Warning: No match for E-Mail Address and/or Password.");
        extentLogWithScreenshot(extentTest, Status.FAIL, "User login to the Application is unsuccessful.",
                takeScreenshot(page));
        return !isErrorDisplayed;
    }
}
