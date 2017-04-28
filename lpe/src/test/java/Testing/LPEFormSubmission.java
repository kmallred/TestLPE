package Testing;

import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterTest;

public class LPEFormSubmission {
	private Properties prop;
	private String accessToken, lids, lnames;
	// This program is used to identify the elements under form tag and to
	// populate them and post a lead...
	WebDriver driver;
	String browser;
  @Test
  @Parameters("browser")
  public void lpeFormSubmit(String browser) throws IOException {
	
		
			//Load Property file for getting Salesforce instance details
			loadPropertyFile();
			//Use RestAssured Api to connect to Salesforce and retreive AccessToken
			accessToken = getSalesforceAccessToken();
			//Query to fetch Landing Page names and Id's
			Response responseSF = given().header("Authorization", "Bearer " + accessToken).when()
					.contentType(ContentType.JSON)
					.get("https://cs60.salesforce.com/services/data/v35.0/query?q=SELECT Id,Name FROM LandingPage__c ")
					.then().extract().response();

			// Getting count of total records in LandingPage custom object to loop
			int totalSize = responseSF.path("totalSize");

			//Processing for each landing page 
			for (int i = 0; i <= totalSize; i++) {

				lids = responseSF.path("records[" + i + "].Id");
				lnames = responseSF.path("records[" + i + "].Name");
				// String URL =
				// "http://landers.beta.glasspanel.com/Home?lid=a1F16000002BmMjEAK&Campaign__c=a0W1600000KvLiOEAV&Product__c=a0B16000016147eEAA";
				String URL = "http://landers.beta.glasspanel.com/" + lnames + "?lid=" + lids
						+ "&Campaign__c=a0W3C00000016AzUAI&Product__c=a0B3C000001iJxeUAE";
				System.out.println(URL + " on browser " + browser);
				try {

					driver.manage().deleteAllCookies();
					driver.navigate().refresh();
					driver.get(URL);

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

				if (driver.getPageSource().contains("404")) {

					System.out.println("404 Error - Landing Page Broken for lander " + URL + " on browser " + browser);
					File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(scrFile, new File("c:\\Kalpana\\Defect Screenshots Selenium\\screenshot_" + lnames
							+ "_" + lids + "_" + browser + ".png"));
					continue;
				}

				if (!isElementPresent(By.name("vm.form"))) {
					System.out.println("No form on the lander");
					File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(scrFile, new File("c:\\Kalpana\\Defect Screenshots Selenium\\lander_" + lnames + "_"
							+ lids + "_" + browser + ".png"));
					continue;
				}

				WebElement formElement = driver.findElement(By.name("vm.form"));

				List<WebElement> allElementsOnFirstPage = formElement
						.findElements(By.xpath("//ng-form[@name='formly_0']/div"));

			//	System.out.println("FirstPage Elements" + allElementsOnFirstPage.size());
				int firstPageSelectCount = 0;
				int firstPageInputCount = 0;

				for (int k = 0; k < allElementsOnFirstPage.size(); k++) {

					if (allElementsOnFirstPage.get(k).getAttribute("class")
							.equals("formly-field ng-scope ng-isolate-scope formly-field-picklist")) {
						firstPageSelectCount = firstPageSelectCount + 1;
						WebElement selectList = formElement.findElement(By
								.xpath("//ng-form[@name='formly_0']/div[@class='formly-field ng-scope ng-isolate-scope formly-field-picklist']["
										+ firstPageSelectCount + "]/div/div//select"));
						Select select = new Select(selectList);
						select.selectByIndex(1);

					}
					if (allElementsOnFirstPage.get(k).getAttribute("class")
							.equals("formly-field ng-scope ng-isolate-scope formly-field-input")) {

						firstPageInputCount = firstPageInputCount + 1;
						WebElement inputText = formElement.findElement(By
								.xpath("//ng-form[@name='formly_0']/div[@class='formly-field ng-scope ng-isolate-scope formly-field-input']["
										+ firstPageInputCount + "]/div/div//input"));

						String inputId = inputText.getAttribute("id");

						if (inputId.equalsIgnoreCase("Email")) {
							formElement.findElement(By.id(inputId)).sendKeys("test@gmail.com");
						} else if (inputId.equalsIgnoreCase("Phone")) {
							formElement.findElement(By.id(inputId)).sendKeys("1212");
						} else if (inputId.equalsIgnoreCase("Postal")) {
							formElement.findElement(By.id(inputId)).sendKeys("12121");
						} else
							formElement.findElement(By.id(inputId)).sendKeys(inputId + "test");
					}

				}

				boolean submitButtonPresent = isElementPresent(
						By.xpath("//ng-form[@name='formly_0']//button[@ng-if='member.hassubmitbutton']"));
				boolean nextButtonPresent = isElementPresent(
						By.xpath("//ng-form[@name='formly_0']//button[@ng-if='member.hasnextbutton']"));
				if (submitButtonPresent) {
					//formElement.findElement(By.xpath("//button[@ng-if= 'member.hassubmitbutton']")).click();
					continue;
				} else if (nextButtonPresent) {
					formElement.findElement(By.xpath("//button[@ng-if= 'member.hasnextbutton']")).click();
					int secondPageSelectCount = 0;
					int secondPageInputCount = 0;
					List<WebElement> allElementsOnSecondPage = formElement
							.findElements(By.xpath("//ng-form[@name='formly_1']/div"));
					//System.out.println("SecondPage Elements" + allElementsOnSecondPage.size());
					for (int l = 0; l < allElementsOnSecondPage.size(); l++) {

						if (allElementsOnSecondPage.get(l).getAttribute("class")
								.equals("formly-field ng-scope ng-isolate-scope formly-field-picklist")) {
							secondPageSelectCount = secondPageSelectCount + 1;
							WebElement selectListSec = formElement.findElement(By
									.xpath("//ng-form[@name='formly_1']/div[@class='formly-field ng-scope ng-isolate-scope formly-field-picklist']["
											+ secondPageSelectCount + "]/div/div//select"));
							Select select = new Select(selectListSec);
							select.selectByIndex(1);
						}
						if (allElementsOnSecondPage.get(l).getAttribute("class")
								.equals("formly-field ng-scope ng-isolate-scope formly-field-input")) {
							secondPageInputCount = secondPageInputCount + 1;
							WebElement inputTextSec = formElement.findElement(By
									.xpath("//ng-form[@name='formly_1']/div[@class='formly-field ng-scope ng-isolate-scope formly-field-input']["
											+ secondPageInputCount + "]/div/div//input"));

							String inputIdSec = inputTextSec.getAttribute("id");

							if (inputIdSec.equalsIgnoreCase("Email")) {
								formElement.findElement(By.id(inputIdSec)).sendKeys("test@gmail.com");
							} else if (inputIdSec.equalsIgnoreCase("Phone")) {
								formElement.findElement(By.id(inputIdSec)).sendKeys("1212");
							} else if (inputIdSec.equalsIgnoreCase("Postal")) {
								formElement.findElement(By.id(inputIdSec)).sendKeys("12121");
							} else
								formElement.findElement(By.id(inputIdSec)).sendKeys(inputIdSec + "test");

						}
					}

				}
				// formElement.findElement(By.xpath("//button[@id='submit-form']")).click();
			}
		}
 
  
  
  @BeforeTest
  @Parameters("browser")
  public void setup(String browser) {
		if (browser.equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver", "Drivers\\chromedriver.exe");
			driver = new ChromeDriver();
		}
		if (browser.equalsIgnoreCase("ie")) {
			System.setProperty("webdriver.ie.driver", "Drivers\\IEDriverServer.exe");
			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
			caps.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			// caps.setCapability("ignoreProtectedModeSettings" , true);
			// Setting attribute nativeEvents to false enable click button in IE
			caps.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			// clear_cache_before_launch
			caps.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			try {

				Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 255");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver = new InternetExplorerDriver(caps);
			driver.manage().deleteAllCookies();

		}

		if (browser.equalsIgnoreCase("FireFox")) {
			System.setProperty("webdriver.gecko.driver", "Drivers\\geckodriver.exe");
			/*
			 * DesiredCapabilities capsFF = DesiredCapabilities.firefox();
			 * capsFF.setCapability(FirefoxDriver.PROFILE, "AutomationProfile");
			 */
			ProfilesIni profile = new ProfilesIni();

			FirefoxProfile myprofile = profile.getProfile("AutomationProfile");

			driver = new FirefoxDriver(myprofile);
			// driver = new FirefoxDriver();
		}

		driver.manage().window().maximize();
	}

  @AfterTest
  public void closeBrowser() {
		driver.quit();
	}

  private void loadPropertyFile() throws FileNotFoundException {

		FileInputStream input = new FileInputStream("config.properties");
		prop = new Properties();
		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getSalesforceAccessToken() {

		String env = prop.getProperty("sfEnvProd");
		String client_id = prop.getProperty("sfClientIDProd");
		String client_secret = prop.getProperty("sfClientSecretProd");
		String username = prop.getProperty("sfUserNameProd");
		String password = prop.getProperty("sfPasswordProd");
		String security_token = prop.getProperty("sfSecurityTokenProd");

		String URI = env + "services/oauth2/token?grant_type=password&client_id=" + client_id + "&client_secret="
				+ client_secret + "&username=" + username + "&password=" + password + security_token;

		Response response = RestAssured.post(URI);
		String accessToken = response.getBody().path("access_token").toString();
		// System.out.println(accessToken);
		return accessToken;
	}

	public boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
