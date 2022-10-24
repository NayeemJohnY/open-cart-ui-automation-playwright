package tests;

import static base.PlaywrightFactory.takeScreenshot;
import static utils.ExtentReporter.extentLogWithScreenshot;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;

import base.PlaywrightFactory;
import pages.HomePage;
import pages.LoginPage;
import utils.ExtentReporter;
import utils.TestProperties;

/**
 * Base Class for the TestNG test
 */
public class TestBase {

	protected Page page;
	protected SoftAssert softAssert = new SoftAssert();
	protected ExtentTest extentTest, testNode;
	protected HomePage homePage;
	protected LoginPage loginPage;
	protected static final String HOME_PAGE_TITLE = "Your Store";
	protected static ExtentReports reporter;
	protected static TestProperties testProperties;
	private static Logger log;

	/**
	 * BeforeSuite method to clean up the test-results directory and initialize the
	 * extent reporter, logger and read test properties
	 * 
	 * @throws Exception
	 */
	@BeforeSuite
	public void setupBeforeTestSuite() throws Exception {
		File file = new File("test-results");
		if (file.exists() && !deleteDirectory(file)) {
			throw new Exception("Exception occurred while deleting test-results directory");
		}
		log = LogManager.getLogger();
		testProperties = new TestProperties();
		testProperties.updateTestProperties();
		reporter = ExtentReporter.getExtentReporter(testProperties);
	}

	/**
	 * AfterSuite method to assert all the soft assertions and flush(write) the
	 * extent report
	 */
	@AfterSuite
	public void teardownAfterTestSuite() {
		try {
			softAssert.assertAll();
			reporter.flush();
		} catch (Exception e) {
			log.error("Error in AfterSuite Method ", e);
		}
	}

	/**
	 * BeforeMethod to start the playwright server, create page and navigate to the
	 * base URL
	 */
	@BeforeMethod
	public void startPlaywrightServer() {
		PlaywrightFactory pf = new PlaywrightFactory(testProperties);
		page = pf.createPage();
		page.navigate(testProperties.getProperty("url"));
	}

	/**
	 * AfterMethod to stop the tracing if enabled and save the tracing
	 * and add screenshot for tests which result is not SUCCESS
	 * 
	 * @param result - {@link ITestResult} of current Test
	 */
	@AfterMethod
	public void closePage(ITestResult result) {
		String testName = testNode.getModel().getName().replaceAll("[^A-Za-z0-9_\\-\\.\\s]", "");
		if (Boolean.parseBoolean(testProperties.getProperty("enableTracing"))) {
			String fileName = testProperties.getProperty("tracingDirectory") + "Trace_"
					+ testName + ".zip";
			page.context().tracing().stop(new Tracing.StopOptions()
					.setPath(Paths.get(fileName)));
		}
		if (!result.isSuccess())
			extentLogWithScreenshot(testNode, Status.WARNING, "The test is not Passed. Please refer the previous step.",
					takeScreenshot(page));
		page.context().browser().close();
		reporter.flush();
	}

	/**
	 * Method to delete the directory recursively
	 * 
	 * @param directoryToBeDeleted - {@link File} to be deleted
	 * @return boolean - Returns {@link Boolean} of delete operation
	 */
	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}
