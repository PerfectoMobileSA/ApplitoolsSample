package utils;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.Eyes;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.exception.ReportiumException;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import pages.LoginPage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppliBase {

    protected static Eyes eyes;
    protected static BatchInfo batchInfo;
    private final String VERSION = "v2";  // Put your preferred v1/v2 demo website version/ pass it a -D System property from Maven
    private final String CLOUD_NAME = "ps";  // Put your perfecto cloud name here/ pass it a -D System property from Maven
    private final String SECURITY_TOKEN = "<<SECURITY TOKEN>>"; // Put your Perfecto security Key here/ pass it a -D System property from Maven
    private final String API_KEY = "<<APPLITOOLS API KEY>>"; // Put your applitools API Key here/ pass it a -D System property from Maven
    private final String MODEL = "iPhone-8"; // Put your preferred device model here/ pass it a -D System property from Maven
    protected WebDriver driver;
    protected ReportiumClient reportiumClient = null;
    protected LoginPage loginPage;
    protected HomePage homePage;

    public static boolean validatePDF(String filepath) throws IOException, InterruptedException {
        //ImageTester.jar is used to validate PDF's in applitools
        String command = String.format(
                "java -jar resources/ImageTester.jar -k %s -f %s",
                System.getProperty("APPLITOOLS_KEY"), filepath);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        String stream = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        System.out.println(stream);
		return stream == null || !stream.contains("Mismatch");
	}

    @BeforeClass(alwaysRun = true)
    public void baseBeforeClass() {
        String cloudName = System.getProperty("cloudName", CLOUD_NAME);
        String securityToken = System.getProperty("securityToken", SECURITY_TOKEN);
        //Perfecto's desired capabilities are set to open any available iPhone-8 device.
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("securityToken", securityToken);
        capabilities.setCapability("model", MODEL); // Change the device model as per your needs
        try {
            driver = new RemoteWebDriver(new URL("https://" + cloudName + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
        } catch (Exception e) {
            throw new RuntimeException("Driver not initialized" + e.getMessage());
        }
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        //Setup Applitools Eyes
        eyes = new Eyes();
        // Applitools key and batch details are set. Set the APPLITOOLS_KEY as a -D System property from Maven/ hardcode it here
        eyes.setApiKey(System.getProperty("APPLITOOLS_KEY", API_KEY));
        eyes.setBatch(new BatchInfo(TestData.batchName));
        eyes.setSendDom(false);
    }

    private ReportiumClient getReportiumClient(WebDriver driver) {
        // Reporting client. For more details, see https://developers.perfectomobile.com/display/PD/Java
        PerfectoExecutionContext perfectoExecutionContext;
        if (System.getProperty("reportium-job-name") != null) {
            perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
                                               .withProject(new Project("My Project", "1.0"))
                                               .withJob(new Job(System.getProperty("reportium-job-name"), Integer.parseInt(System.getProperty("reportium-job-number"))))
                                               .withContextTags("tag1")
                                               .withWebDriver(driver)
                                               .build();
        } else {
            perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
                                               .withProject(new Project("My Project", "1.0"))
                                               .withContextTags("tag1")
                                               .withWebDriver(driver)
                                               .build();
        }
        return new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeBaseMethod(Method method) {
        // Setup Perfecto's Smart Reporting
        reportiumClient = getReportiumClient(driver);
        try {
            Class<?> testClass = method.getDeclaringClass();
            String testName = testClass.getSimpleName() + "." + method.getName();
            System.out.println("running: " + method.getName() + " of class:" + testClass.getSimpleName());
            reportiumClient.testStart(testName, new TestContext(method.getName(), testClass.getSimpleName()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        reportiumClient.stepStart("Browser Navigation");
        //Launch the preferred website
        if (System.getProperty("version", VERSION).equalsIgnoreCase("v1")) {
            driver.get(TestData.v1);
        } else {
            driver.get(TestData.v2);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterBaseMethod(ITestResult testResult) {
        //Setup appropriate result status in Perfecto's Smart Reporting
        int status = testResult.getStatus();
        switch (status) {
            case ITestResult.FAILURE:
                reportiumClient.testStop(TestResultFactory.createFailure("An error occurred", testResult.getThrowable()));
                break;
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
            case ITestResult.SUCCESS:
                reportiumClient.testStop(TestResultFactory.createSuccess());
                break;
            case ITestResult.SKIP:
                // Ignore
                break;
            default:
                throw new ReportiumException("Unexpected status: " + status);
        }
    }

    @AfterClass(alwaysRun = true)
    public void baseAfterClass() {
        System.out.println("Report url: " + reportiumClient.getReportUrl());
        //Kill the driver
        if (driver != null) {
            driver.quit();
        }
        // Kill Applitools eyes if not closed
        if (eyes != null) {
            eyes.abortIfNotClosed();
        }
    }

    public void checkWindow(String tag, String matchLevel) {
        //Set the preferred match level. Refer here for more info: https://help.applitools.com/hc/en-us/articles/360007188591-…
        switch (matchLevel.toUpperCase()) {
            case "STRICT":
                eyes.setMatchLevel(MatchLevel.STRICT);
                break;
            case "CONTENT":
                eyes.setMatchLevel(MatchLevel.CONTENT);
                break;
            case "LAYOUT":
                eyes.setMatchLevel(MatchLevel.LAYOUT);
                break;
            case "EXACT":
                eyes.setMatchLevel(MatchLevel.EXACT);
                break;
            default:
                break;
        }
        // Set eyes options
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setHostOS(getDevicePropertyInfo("os"));
        eyes.setHostApp(getDevicePropertyInfo("Model") + " _ " + getDevicePropertyInfo("osVersion"));
        eyes.getForceFullPageScreenshot();
        reportiumClient.stepStart("Eyes on: " + tag);
        // Open eyes
        eyes.open(driver, TestData.appName, Thread.currentThread().getStackTrace()[2].getMethodName());
        // Use Eyes' checkWindow method
        try {
            eyes.checkWindow(tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Close eyes and update results in perfecto's Smart Reporting
        TestResults end = null;
        try {
            end = eyes.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reportiumClient.stepStart("Results:" + tag);
		reportiumClient.reportiumAssert(end.getUrl(), end.isPassed());
    }

    public String getDevicePropertyInfo(String propertyName) {
        //Retrieves device info
        Map<String, Object> params = new HashMap<>();
        params.put("property", propertyName);
        String result = (String) ((RemoteWebDriver) driver).executeScript("mobile:handset:info", params);
        return result;
    }

    public void swipeUp(WebDriver driver) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", "50%,90%");
        params.put("end", "50%,20%");
        params.put("duration", "1");
        ((RemoteWebDriver) driver).executeScript("mobile:touch:swipe", params);
    }
}