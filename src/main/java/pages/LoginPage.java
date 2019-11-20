package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {

	private WebDriver driver;

	@FindBy(id = "username")
	public WebElement usernameField;

	@FindBy(id = "password")
	public WebElement passwordField;

	@FindBy(id = "log-in")
	public WebElement signIn;

	public LoginPage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	public void login(String username, String password) {
		usernameField.clear();
		passwordField.clear();
		usernameField.sendKeys(username);
		passwordField.sendKeys(password);
		signIn.click();
	}
	
	public void loginIfRequired(String username, String password) {
		List<WebElement> elements = driver.findElements(By.id("log-in"));
		if (!elements.isEmpty()) {
			login(username, password);
		}
	}
}