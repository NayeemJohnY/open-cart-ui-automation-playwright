package testutils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 1;
    private int maxRetryCount = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {

        if (retryCount <= maxRetryCount) {
            // Add custom attribute for retry
            iTestResult.getTestContext().setAttribute("retryCount", retryCount);
            retryCount++;
            iTestResult.setStatus(ITestResult.FAILURE);
            return true;
        } else {
            iTestResult.setStatus(ITestResult.FAILURE);
        }
        return false;
    }
}
