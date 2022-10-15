package utils;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReporter {

    private static Logger log = LogManager.getLogger();

    private ExtentReporter() {
        throw new IllegalStateException("Extent Reporter class instantiation is not allowed");
    }

    /**
     * Method to configure and get the ExtentReporter instance
     * 
     * @return ExtentReports - Returns {@link ExtentReports} instance
     * @throws IOException - Throws {@link IOException}
     */
    public static ExtentReports getExtentReporter(TestProperties testProperties) throws IOException {
        ExtentSparkReporter reporter = new ExtentSparkReporter(testProperties.getProperty("extentReportPath"));
        reporter.loadXMLConfig("./src/main/resources/extent-report-config.xml");

        reporter.config().setCss("img.r-img { width: 30%; }");
        ExtentReports extentReports = new ExtentReports();
        extentReports.attachReporter(reporter);

        String applicationURL = "<a href=\"" + testProperties.getProperty("url")
                + "\" target=\"_blank\">Open cart Demo Application</a>";
        extentReports.setSystemInfo("Application", applicationURL);

        extentReports.setSystemInfo("OS", System.getProperties().getProperty("os.name"));
        extentReports.setSystemInfo("Browser", testProperties.getProperty("browser"));

        if (Boolean.getBoolean(testProperties.getProperty("enableRecordVideo"))) {
            String filePath = Paths.get(testProperties.getProperty("recordVideoDirectory")).toAbsolutePath()
                    .toString();
            String recordedVideoFilePath = "<a href=\"" + filePath
                    + "\" target=\"_blank\"Open cart Demo Application</a>";
            extentReports.setSystemInfo("Execution Recorded Video", recordedVideoFilePath);
        }
        return extentReports;
    }

    /**
     * Method to add the log the step to extent report
     * 
     * @param extentTest - {@link ExtentTest}
     * @param status     - {@link Status}
     * @param message    - {@link String} log message
     */
    public static void extentLog(ExtentTest extentTest, Status status, String message) {
        extentTest.log(status, message);
        log(status, message);
    }

    /**
     * Method to add the log step with the screenshot to the extent report
     * 
     * @param extentTest - {@link ExtentTest}
     * @param status     - {@link Status}
     * @param message    - {@link String} log message
     * @param base64Path - {@link java.util.Base64} {@link String} of screenshot
     */
    public static void extentLogWithScreenshot(ExtentTest extentTest, Status status, String message,
            String base64Path) {
        String imageElement = "<br/><img class='r-img' src='data:image/png;base64," + base64Path
                + "' href='data:image/png;base64," + base64Path + "'data-featherlight='image'>";
        extentTest.log(status, message + imageElement);
        log(status, message);
    }

    /**
     * Method to log the message to console and log file.
     * It removes any HTML element in the message before printing logging
     * 
     * @param status  - {@link Status}
     * @param message - {@link String} log message
     */
    private static void log(Status status, String message) {
        message = message.replaceAll("\\<.*?\\>", "");
        switch (status) {
            case FAIL:
                log.error(message);
                break;
            case WARNING:
                log.warn(message);
                break;
            case SKIP:
                log.warn(message);
                break;
            case INFO:
                log.info(message);
                break;
            default:
                log.debug(message);
                break;
        }
    }

}
