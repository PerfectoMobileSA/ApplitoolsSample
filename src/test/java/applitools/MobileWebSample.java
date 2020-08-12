package applitools;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.applitools.eyes.selenium.Eyes;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;

import utils.Utils;

public class MobileWebSample {

	RemoteWebDriver driver;
	ReportiumClient reportiumClient;	
	Eyes eyes;
	private final String CLOUD_NAME = "mobilecloud";  // Put your perfecto cloud name here/ pass it a -D System property from Maven
	private final String SECURITY_TOKEN = "<<SECURITY TOKEN>>"; // Put your Perfecto security Key here/ pass it a -D System property from Maven
	private final String iOS_version = "13.*";
	@Test
	public void seleniumTest() throws Exception {

		String cloudName = System.getProperty("cloudName", CLOUD_NAME);
		String securityToken = System.getProperty("securityToken", SECURITY_TOKEN);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("platformName", "iOS");
		capabilities.setCapability("model", "iPhone.*");
		capabilities.setCapability("browserName", "Safari");
		capabilities.setCapability("platformVersion", System.getProperty("iOS_version", iOS_version));
		capabilities.setCapability("openDeviceTimeout", 2);
		capabilities.setCapability("securityToken", Utils.fetchSecurityToken(securityToken));
		capabilities.setCapability("useAppiumForWeb", true);
		try{
			driver = new RemoteWebDriver(new URL("https://" + Utils.fetchCloudName(cloudName) + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
		}catch(SessionNotCreatedException e){
			throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
		}

		reportiumClient = Utils.setReportiumClient(driver, reportiumClient); //Creates reportiumClient
		reportiumClient.testStart("Applitools iOS web sample", new TestContext("iOS", "applitools")); //Starts the reportium test
		reportiumClient.stepStart("Launch laduree");
		driver.get("https://laduree.goldbelly.com/");
		reportiumClient.stepEnd();
		
		reportiumClient.stepStart("Proceed to checkout");
		try {
			driver.findElement(By.xpath(("//*[@class='cart__count js__header-cart-items-count active' and text() > 0]")));
		}catch(NoSuchElementException e) {
			WebElement item = driver.findElement(By.xpath("(//*[contains(@class,'index-module__productName')])[1]"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", item);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", item);
			driver.findElement(By.xpath("(//button[text()='Add to Cart'])[1]")).click();
			driver.findElement(By.xpath(("//*[@class='cart__count js__header-cart-items-count active' and text() > 0]")));
		}
		driver.findElement(By.xpath(("//*[@class='cart__count js__header-cart-items-count active' and text() > 0]"))).click();
		reportiumClient.reportiumAssert("CheckOut", driver.findElement(By.xpath(("//*[text()='Check Out']"))).isDisplayed());
		reportiumClient.stepEnd();
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		//STOP TEST
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
}