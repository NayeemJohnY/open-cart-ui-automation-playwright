package base;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import utils.TestProperties;

/**
 * The class PlaywrightFactory provides a constructor which starts the
 * playwright server.
 * It has private and public methods to create a playwright page.
 * 
 * @author Nayeem John
 */
public class PlaywrightFactory {

    private static Logger log = LogManager.getLogger();
    private Playwright playwright;
    private TestProperties testProperties;

    /**
     * Constructor to initialize the test properties and playwright server
     * 
     * @param testProperties - {@link TestProperties}
     */
    public PlaywrightFactory(TestProperties testProperties) {
        this.testProperties = testProperties;
        playwright = Playwright.create();
    }

    /**
     * Method is to get playwright {@link Browser} instance of browser property in
     * config file with headless mode property
     * 
     * @return Browser - Returns playwright {@link String} instance
     * @throws IllegalArgumentException - Throws Exception when no matching browser
     *                                  is available for property
     */
    private Browser getBrowser() throws IllegalArgumentException {
        String browserName = testProperties.getProperty("browser");
        boolean headless = Boolean.parseBoolean(testProperties.getProperty("headless"));
        LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);
        BrowserType browserType;
        switch (browserName.toLowerCase()) {
            case "chromium":
                browserType = playwright.chromium();
                break;
            case "firefox":
                browserType = playwright.firefox();
                break;
            case "safari":
                browserType = playwright.webkit();
                break;
            case "chrome":
                browserType = playwright.chromium();
                launchOptions.setChannel("chrome");
                break;
            case "edge":
                browserType = playwright.chromium();
                launchOptions.setChannel("msedge");
                break;
            default:
                String message = "Browser Name '" + browserName + "' specified in Invalid.";
                message += " Please specify one of the supported browsers [chromium, firefox, safari, chrome, edge].";
                log.debug(message);
                throw new IllegalArgumentException(message);
        }
        log.info("Browser Selected for Test Execution '{}' with headless mode as '{}'", browserName, headless);
        return browserType.launch(launchOptions);
    }

    /**
     * Method to get the playwright {@link BrowserContext} with the video recording,
     * tracing. storage context and view port
     * These properties are set based on values on config properties
     * 
     * @return BrowserContext - Returns playwright {@link BrowserContext} instance
     */
    private BrowserContext getBrowserContext() {
        BrowserContext browserContext;
        Browser browser = getBrowser();
        NewContextOptions newContextOptions = new Browser.NewContextOptions();

        if (Boolean.parseBoolean(testProperties.getProperty("enableRecordVideo"))) {
            Path path = Paths.get(testProperties.getProperty("recordVideoDirectory"));
            newContextOptions.setRecordVideoDir(path);
            log.info("Browser Context - Video Recording is enabled at location '{}'", path.toAbsolutePath());
        }

        int viewPortHeight = Integer.parseInt(testProperties.getProperty("viewPortHeight"));
        int viewPortWidth = Integer.parseInt(testProperties.getProperty("viewPortWidth"));
        newContextOptions.setViewportSize(viewPortWidth, viewPortHeight);
        log.info("Browser Context - Viewport Width '{}' and Height '{}'", viewPortWidth, viewPortHeight);

        if (Boolean.parseBoolean(testProperties.getProperty("useSessionState"))) {
            Path path = Paths.get(testProperties.getProperty("sessionState"));
            newContextOptions.setStorageStatePath(path);
            log.info("Browser Context - Used the Session Storage State at location '{}'", path.toAbsolutePath());
        }

        browserContext = (browser.newContext(newContextOptions));

        if (Boolean.parseBoolean(testProperties.getProperty("enableTracing"))) {
            browserContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            log.info("Browser Context - Tracing is enabled with Screenshots and Snapshots");
        }
        return browserContext;
    }

    /**
     * Method to create a new playwright {@link Page} for the browser
     * 
     * @return Page - Returns playwright {@link Page} instance or null if any
     *         exception occurs while retrieving {@link BrowserContext}
     */
    public Page createPage() {
        Page page = null;
        try {
            page = (getBrowserContext().newPage());
        } catch (Exception e) {
            log.error("Unable to create Page : ", e);
        }
        return page;
    }

    /**
     * Method to save the session state from the {@link BrowserContext} in a file
     * provided in 'sessionState' property
     * 
     * @param page     - playwright {@link Page} instance
     * @param filename - {@link String} name of the file to store session state
     */
    public static void saveSessionState(Page page, String filename) {
        page.context().storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get(filename)));
    }

    /**
     * Method to take screenshot of the {@link Page}
     * It saves the screenshots with file name of ${currentTimeMillis}.png
     * 
     * @param page - playwright {@link Page} instance
     * @return String - Returns encoded {@link Base64} String of image
     */
    public static String takeScreenshot(Page page) {
        String path = System.getProperty("user.dir") + "/test-results/screenshots/" + System.currentTimeMillis()
                + ".png";

        byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)).setFullPage(true));
        String base64Path = Base64.getEncoder().encodeToString(buffer);

        log.debug("Screenshot is taken and saved at the location  {}", path);
        return base64Path;
    }
}
