package applitools;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.Eyes;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import utils.Utils;

public class Perfecto_Native_Applitools {
	AppiumDriver<?> driver;
	ReportiumClient reportiumClient;
	Eyes eyes;

	@Test
	public void appiumTest() throws Exception {
		//Replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>  
		String cloudName = "<<cloud name>>";
		//Replace <<security token>> with your perfecto security token or pass it as maven properties: -DsecurityToken=<<SECURITY TOKEN>>  More info: https://developers.perfectomobile.com/display/PD/Generate+security+tokens
		String securityToken = "<<security token>>";

		//A sample perfecto connect appium script to connect with a perfecto android device and perform addition validation in calculator app.
		String browserName = "mobileOS";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		capabilities.setCapability("securityToken", Utils.fetchSecurityToken(securityToken));
		capabilities.setCapability("model", "Galaxy S6");
		capabilities.setCapability("enableAppiumBehavior", true);
		capabilities.setCapability("openDeviceTimeout", 2);
		capabilities.setCapability("appPackage", "com.sec.android.app.popupcalculator");
		capabilities.setCapability("appActivity", "com.sec.android.app.popupcalculator.Calculator");	
		try{
			driver = new AndroidDriver<>(new URL("https://" + Utils.fetchCloudName(cloudName)  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities); 
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			System.out.println("\n"+driver.getCapabilities());
		}catch(SessionNotCreatedException e){
			throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
		}

		reportiumClient = Utils.setReportiumClient(driver, reportiumClient); //Creates reportiumClient
		reportiumClient.testStart("My Calculator Test", new TestContext("tag2", "tag3")); //Starts the reportium test

		reportiumClient.stepStart("Verify Calculator App is loaded"); //Starts a reportium step
		driver.findElement(By.xpath("//*[@resource-id=\"com.sec.android.app.popupcalculator:id/bt_add\"]")).isDisplayed();
		
        eyes.open(driver, "Calculator", "Calculator Test");
        eyes.checkWindow("Calculator!");
        reportiumClient.stepEnd(); //Stops a reportium step
		
		reportiumClient.stepStart("Click something");
		driver.findElement(By.xpath("//*[@text='1']")).click();
		
		eyes.checkWindow("Something!");
		reportiumClient.stepEnd();
		
		System.out.println("\n" + driver.getCapabilities());
	}
	
	@BeforeSuite
	public void setup() {
		eyes = new Eyes();
        eyes.setApiKey(System.getProperty("APPLITOOLS_KEY"));
        eyes.setMatchLevel(MatchLevel.STRICT);
        String batchName = "appium_native_test-sample-" + new Date().toString();
        eyes.setBatch(new BatchInfo(batchName));
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setForceFullPageScreenshot(false);
	}
	

	@AfterMethod
	public void afterMethod(ITestResult result) {
		TestResult testResult = null;
		if(result.getStatus() == result.SUCCESS) {
			testResult = TestResultFactory.createSuccess();
		}
		else if (result.getStatus() == result.FAILURE) {
			testResult = TestResultFactory.createFailure(result.getThrowable());
		}
		reportiumClient.testStop(testResult);

		driver.close();
		driver.quit();
		// Retrieve the URL to the DigitalZoom Report 
		String reportURL = reportiumClient.getReportUrl();
		System.out.println(reportURL);
	}

	@AfterSuite
	public void end() {
		System.out.print(">>>>>> END <<<<<<<<<\n");
		TestResults close = eyes.close();
		System.out.println(">>>>>> Applitools report URL: "+ close.getUrl());
	    // If the test was aborted before eyes.close was called, ends the test as aborted.
	    eyes.abortIfNotClosed();
	}
}