package applitools;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.Eyes;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import utils.Utils;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MobileWebSample {

    private final String CLOUD_NAME = "<CLOUD_NAME>";  // Put your perfecto cloud name here/ pass it a -D System property from Maven
    private final String SECURITY_TOKEN = "<PERFECTO_TOKEN>"; // Put your Perfecto security Key here/ pass it a -D System property from Maven
    private final String suffix = "7";
    private final String batchName = "perfecto-ios" + suffix;
    private final String appName = "ladureeTest";
    private final String cloudName = System.getProperty("cloudName", CLOUD_NAME);
    private final String securityToken = System.getProperty("securityToken", SECURITY_TOKEN);
    private final BatchInfo batchInfo = new BatchInfo(batchName);
    private RemoteWebDriver driver;
    private ReportiumClient reportiumClient;

    @Test
    public void seleniumTestv12() throws Exception {
        String iOS_version = "12.*";
        String testName = "seleniumTestv12";
        System.out.println("Running test: " + testName);

        DesiredCapabilities capabilities = getDesiredCapabilities(iOS_version);
        driver = createDriver(capabilities);

        Eyes eyes = instantiateEyes(testName);
        eyes.open(driver, appName, testName);

        reportiumClient = Utils.setReportiumClient(driver, reportiumClient); //Creates reportiumClient
        reportiumClient.testStart("Applitools iOS web sample - " + testName, new TestContext("iOS", "applitools")); //Starts the reportium test
        reportiumClient.stepStart("Launch laduree");
        driver.get("https://laduree.goldbelly.com/");
        eyes.checkWindow("home");
        reportiumClient.stepEnd();

        reportiumClient.stepStart("Proceed to Home Scents");

        driver.get("https://laduree.goldbelly.com/categories/Home-Scents");
        eyes.checkWindow("Home-Scents");
        reportiumClient.stepEnd();

        reportiumClient.stepStart("Proceed to Gifts");
        driver.get("https://laduree.goldbelly.com/categories/Gifts");
        eyes.checkWindow("Gifts");
        reportiumClient.stepEnd();

        TestResults visualTestResults = eyes.close();
        System.out.println("Visual Testing results - " + visualTestResults);
    }

    private RemoteWebDriver createDriver(DesiredCapabilities capabilities) throws Exception {
        try {
            RemoteWebDriver driver = new RemoteWebDriver(new URL("https://" + Utils.fetchCloudName(cloudName) + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
            driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
            return driver;
        } catch (SessionNotCreatedException e) {
            throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
        }
    }

    @NotNull
    private Eyes instantiateEyes(String testName) {
        System.out.println("Creating eyes for test: " + testName);
        Eyes eyes = new Eyes();
        eyes.setApiKey(System.getenv("APPLITOOLS_API_KEY"));
        eyes.setMatchLevel(MatchLevel.STRICT);
        eyes.setBranchName("main");
        eyes.setBatch(batchInfo);
        return eyes;
    }

    @NotNull
    private DesiredCapabilities getDesiredCapabilities(String iOS_version) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("model", "iPhone-" + suffix);
        capabilities.setCapability("browserName", "Safari");
        capabilities.setCapability("platformVersion", System.getProperty("iOS_version", iOS_version));
        capabilities.setCapability("openDeviceTimeout", 2);
        capabilities.setCapability("securityToken", Utils.fetchSecurityToken(securityToken));
        capabilities.setCapability("useAppiumForWeb", true);
        return capabilities;
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        //STOP TEST
        TestResult testResult = null;

        if (result.getStatus() == result.SUCCESS) {
            testResult = TestResultFactory.createSuccess();
        } else if (result.getStatus() == result.FAILURE) {
            testResult = TestResultFactory.createFailure(result.getThrowable());
        }
        reportiumClient.testStop(testResult);

        driver.close();
        driver.quit();
        // Retrieve the URL to the DigitalZoom Report
        String reportURL = reportiumClient.getReportUrl();
        System.out.println(reportURL);
    }
}