package pages;

import static base.PlaywrightFactory.takeScreenshot;
import static utils.ExtentReporter.extentLog;
import static utils.ExtentReporter.extentLogWithScreenshot;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object Class for Product Page
 * 
 * @author Nayeem John
 */
public class ProductPage {

    private Page page;
    private ExtentTest extentTest;
    private String productItem = ".product-thumb .caption";
    private String priceParagraphSelector = "p.price";
    private String priceExTaxSelector = "span.price-tax";
    private String priceNew = "span.price-new";
    private String sortInput = "#input-sort";
    private String shareCountSelector = "a.addthis_button_expanded";
    private static Logger log = LogManager.getLogger();

    public ProductPage(Page page, ExtentTest extentTest) {
        this.page = page;
        this.extentTest = extentTest;
    }

    /**
     * Method to get the Products and price from products page
     * The locator name and price is captured using locator chaining
     * The currency character in price will be replaced with empty string in order
     * to parse price
     * 
     * @return Map<String, Double> - Returns {@link Map} of the Product name and
     *         price value (Double)
     */
    private Map<String, Double> getProductsAndPrice() {
        Map<String, Double> mapOfProductAndPrice = new HashMap<>();
        Locator productItems = page.locator(productItem);
        for (int i = 0; i < productItems.count(); i++) {

            String productName = productItems.nth(i).locator("a").textContent();
            // Get the new price if available else price
            Locator priceParagraphLocator = productItems.nth(i).locator(priceParagraphSelector);
            String priceString = priceParagraphLocator.textContent()
                    .replace(priceParagraphLocator.locator(priceExTaxSelector).textContent(), "");
            if (priceParagraphLocator.locator(priceNew).isVisible()) {
                priceString = priceParagraphLocator.locator(priceNew).textContent();
            }
            // Replace any currency characters and parse the string to Double
            Double price = Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
            mapOfProductAndPrice.put(productName, price);
        }
        return mapOfProductAndPrice;
    }

    /**
     * Method to sort the products by price from UI and verify the sorting from code
     * Using playwright selectOption to select drop down. The value from drop down
     * is selected based on elementHandle of text locator
     * 
     * @return boolean - Returns the verification of result of sorting -
     *         {@link Boolean}
     */
    public boolean checkProductSortedByPrice() {
        Map<String, Double> mapProductsAndPriceSortedFromCode = sortMapByValue(getProductsAndPrice());
        page.selectOption(sortInput, page.locator("text='Price (Low > High)'").elementHandle());
        Map<String, Double> mapProductsAndPriceSortedFromUI = getProductsAndPrice();
        if (mapProductsAndPriceSortedFromUI.equals(mapProductsAndPriceSortedFromCode)) {
            extentLog(extentTest, Status.PASS, "The products are sorted by price in UI");
            return true;
        } else {
            extentLogWithScreenshot(extentTest, Status.FAIL, "The Products are not sorted by price in UI",
                    takeScreenshot(page));
            return false;
        }
    }

    /**
     * Method to sort the Map based on the value (ascending order)
     * 
     * @param mapOfProductAndPrice - {@link Map}
     * @return Map<String, Double> - Returns the sorted {@link Map}
     */
    private Map<String, Double> sortMapByValue(Map<String, Double> mapOfProductAndPrice) {
        return mapOfProductAndPrice.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

    /**
     * Method to click and view the product item from product list
     * 
     * @param productName - {@link String}
     */
    public void viewProduct(String productName) {
        page.locator("text='" + productName + "'").click();
        log.info("Clicked on Product to view the Product: '{}'", productName);
    }

    /**
     * Method to share the product to the social platform and verify the share count
     * is incremented
     * 
     * @param socialPlatformName - {@link String}
     * @return - Returns {@code true} of product share count is incremented else
     *         {@code false}
     */
    public boolean shareProductToSocialPlatform(String socialPlatformName, int retryAttempt) {
        int shareCountBefore = getShareCount();
        extentLog(extentTest, Status.INFO, "The share count Before share: " + shareCountBefore);

        try {
            handleSocialPlatformService(socialPlatformName, retryAttempt);
        } catch (Exception e) {
            extentTest.fail(e);
            log.error("Exception : ", e);
            return false;
        }

        int shareCountAfter = getShareCount();
        extentLog(extentTest, Status.INFO, "The share score After share: " + shareCountAfter);

        if (shareCountAfter == (shareCountBefore + 1)) {
            extentLog(extentTest, Status.PASS, "The share score is incremented by 1 score After share.");
            return true;
        }

        extentLog(extentTest, Status.FAIL, "Mismatch in share score After share. <br/> Expected: "
                + (shareCountBefore + 1) + " Actual: " + shareCountAfter);
        return false;
    }

    /**
     * Method to handle the share of product in 3 ways. Default services,
     * services on Hover and services on Mask Dialog
     * This method will handle if any pop up (Window/tab) opened for share. This
     * method will mark warning if no pop up is opened
     * 
     * @param socialPlatformName - {@link String}
     * @param retryAttempt       - {@link Integer}
     */
    private void handleSocialPlatformService(String socialPlatformName, int retryAttempt) {
        try {
            Page popupPage = page.context().waitForPage(() -> {

                if (retryAttempt == 0)
                    selectFromDefaultPlatformService(socialPlatformName);

                if (retryAttempt == 1)
                    selectPlatformServiceFromHoverDialog(socialPlatformName);

                if (retryAttempt == 2)
                    selectPlatformServiceFromShareMaskDialog(socialPlatformName);

            });
            popupPage.waitForLoadState(LoadState.NETWORKIDLE);
            extentLogWithScreenshot(extentTest, Status.PASS,
                    "Page Pop up(Window/Tab) is opened for share. Page title: " + popupPage.title(),
                    takeScreenshot(popupPage));
            popupPage.close();

        } catch (TimeoutError e) {
            extentLogWithScreenshot(extentTest, Status.WARNING,
                    "No Page Pop up(Window/Tab) is opened for share. Check the number of shares validation step",
                    takeScreenshot(page));
            log.error("Page Popup Timeout Error", e);
        }
    }

    /**
     * Method to get the current share count of the product
     * 
     * @return shareCount - Returns the share count of the product
     */
    private int getShareCount() {
        int shareCount = 0;
        log.info("Get the number of shares for the product");

        try {
            String shareTextContent = page.textContent(shareCountSelector).trim();
            if (!shareTextContent.isEmpty())
                shareCount = Integer.parseInt(shareTextContent);
        } catch (TimeoutError e) {
            log.error("Share count selector option is not visible", e);
        }

        return shareCount;
    }

    /**
     * Method to share the product on default social platform visible on page.
     * The Method will handle the default platforms are available in Frames.
     * Here scrollIntoViewIfNeeded() is used to used to mitigate the known issue
     * {@link https://github.com/microsoft/playwright/issues/3166}
     * of playwright not auto scrolls to frame element not in the view.
     * 
     * @param socialPlatformName - {@link String}
     */
    private void selectFromDefaultPlatformService(String socialPlatformName) {
        boolean isPlatformServiceFound = false;
        log.info("Checking the '{}' social platform is available in Default Social Platform Service",
                socialPlatformName);
        if (socialPlatformName.equalsIgnoreCase("facebook")) {
            String facebookSelector = "//*[contains(@title, 'Facebook')]";
            page.waitForSelector(facebookSelector).scrollIntoViewIfNeeded();
            page.frameLocator(facebookSelector).locator("//button[@title='Like']").click();
            isPlatformServiceFound = true;
        }

        if (socialPlatformName.equalsIgnoreCase("twitter")) {
            String twitterSelector = "//*[@id='twitter-widget-0']";
            page.waitForSelector(twitterSelector).scrollIntoViewIfNeeded();
            page.frameLocator(twitterSelector).locator("//*[@id='l']").click();
            isPlatformServiceFound = true;
        }
        if (!isPlatformServiceFound)
            throw new IllegalStateException(
                    "No matching social platform is available in Default Services for : " + socialPlatformName);

    }

    /**
     * Method to share the product on social platform which found on Hover dialog
     * This method will be called when there is no matching platform on default
     * services
     * 
     * @param socialPlatformName - {@link String}
     */
    private void selectPlatformServiceFromHoverDialog(String socialPlatformName) {
        String shareIconSelector = "//a[text()='Share']";
        String platformSelectorString = "//span[contains(@class,'at-label')]";
        log.info("Checking the '{}' social platform is available in Hover Dialog", socialPlatformName);

        page.hover(shareIconSelector);
        try {
            Locator platformServiceLocator = page.locator(platformSelectorString,
                    new Page.LocatorOptions().setHas(page.locator("text='" + socialPlatformName + "'")));
            platformServiceLocator.waitFor();
            platformServiceLocator.click();
        } catch (Exception e) {
            throw new IllegalStateException("No matching social platform is available in Hover Dialog :" + socialPlatformName);
        }
    }

    /**
     * Method to share the product on social platform which opened on Mask dialog
     * This method will be called when there is no matching platform on default
     * services and Hover dialog
     * 
     * @param socialPlatformName - {@link String}
     */
    private void selectPlatformServiceFromShareMaskDialog(String socialPlatformName) {
        String shareIconSelector = "//a[text()='Share']";
        String moreOption = " #atic_more";
        String searchFieldString = "#at-expanded-menu-service-filter";
        String platformSelectorString = "//span[@class='at-icon-name']";
        log.info("Checking the '{}' social platform is available in Mask Dialog", socialPlatformName);

        page.hover(shareIconSelector);
        page.click(moreOption);
        page.waitForSelector(searchFieldString).fill(socialPlatformName);
        Locator socialIconLocator = page.locator(platformSelectorString);
        socialIconLocator.last().waitFor();
        log.info("Social platforms available for search: '{}'", socialIconLocator.allTextContents());

        if (socialIconLocator.count() == 0) {
            extentLogWithScreenshot(extentTest, Status.FAIL,
                    "No Social Platform service found for search: '" + socialPlatformName + "'", takeScreenshot(page));
        }

        if (socialIconLocator.count() == 1) {
            extentLog(extentTest, Status.PASS,
                    "Social Platform service found for search: '" + socialPlatformName + "'");
            socialIconLocator.click();
        } else {
            String message = "";
            if (socialIconLocator.count() == 0)
                message = "No Social Platform service found for search: '" + socialPlatformName + "'";
            
            if (socialIconLocator.count() > 1)
                message = "More than 1 Social Platform services found for search: '" + socialPlatformName + "'. Services found: " + socialIconLocator.allTextContents();
            
            String closePlatformServiceMask = "button.at-expanded-menu-close";
            if (page.isVisible(closePlatformServiceMask))
                page.click(closePlatformServiceMask);
            
            throw new IllegalStateException(message);
        }
    }

}
