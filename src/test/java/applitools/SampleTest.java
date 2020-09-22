package applitools;

import org.junit.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utils.AppliBase;

public class SampleTest extends AppliBase {

    @Test
    //sample to showcase applitools capabilities like missing images, alignments, etc
    public void testLogin() {
        checkWindow("Login Page", "STRICT");
    }

    @Test
    //sample to showcase table verification
    public void testLoggedIn() {
        LoginPage loginPage = new LoginPage(driver);
        reportiumClient.stepStart("Try to Login");
        loginPage.loginIfRequired("username", "somepassword");
        swipeUp(driver);
        checkWindow("Logged-In Page check", "STRICT");
    }

    @Test
    //sample to showcase charts verification
    public void testCharts() {
        LoginPage loginPage = new LoginPage(driver);
        HomePage homePage = new HomePage(driver);
        reportiumClient.stepStart("Try to Login");
        loginPage.loginIfRequired("username", "somepassword");
        homePage.navigateToCanvas();
        checkWindow("Charts test", "LAYOUT");
    }

    @Test
    //sample to showcase pdf verification
    public void testPDF() throws Exception {
        reportiumClient.stepStart("pdf validation");
        String destination;
        if (System.getProperty("version").equalsIgnoreCase("v1")) {
            destination = "resources/v1/pdf.pdf";
        } else {
            destination = "resources/v2/pdf.pdf";
        }
        Assert.assertTrue("Mismatch found while validating PDF", validatePDF(destination));
        if (validatePDF(destination)) {
            reportiumClient.reportiumAssert("PDF: " + destination + " validated successfully", validatePDF(destination));
        } else {
            reportiumClient.reportiumAssert("Mismatch found while validating PDF: " + destination, validatePDF(destination));
        }
        reportiumClient.stepEnd();
    }
}