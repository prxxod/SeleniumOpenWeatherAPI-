import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginAutomationWithReport {

    private static final String FROM_EMAIL = "pramodaskdrive01@gmail.com";
    private static final String PASSWORD = "iyzxadvqmtuihehu";
    private static final String TO_EMAIL = "pramodask2003@gmail.com";
    private static final String SUBJECT = "API Test Result";
    private static WebDriver driver;
    private final List<String> testResults = new ArrayList<>();

    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "D:/Work/things/chromedriver-win64/chromedriver.exe");
        driver = new ChromeDriver();
    }

    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public void readExcelAndLogin(String filePath) throws IOException {
        FileInputStream file = new FileInputStream(new File(filePath));
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            Cell usernameCell = row.getCell(0);
            Cell passwordCell = row.getCell(1);
            String username = usernameCell.getStringCellValue();
            String password = passwordCell.getStringCellValue();
            System.out.println(username);
            System.out.println(password);
            loginToWebsite(username, password);


        }
        workbook.close();
    }

    public void loginToWebsite(String username, String password) {
        driver.get("https://practicetestautomation.com/practice-test-login/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Locate the elements for login
        WebElement usernameField = driver.findElement(By.id("username"));
        usernameField.isDisplayed();
        usernameField.isEnabled();

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.isDisplayed();
        passwordField.isEnabled();

        WebElement loginButton = driver.findElement(By.id("submit"));
        loginButton.isDisplayed();
        loginButton.isEnabled();


        // Perform login

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();



        String currentUrl = driver.getCurrentUrl();
        System.out.println(currentUrl);
        assert currentUrl != null;
        boolean loginSuccessful = currentUrl.contains("https://practicetestautomation.com/logged-in-successfully/");

        // Check if login was successful
//        boolean loginSuccessful = driver.getPageSource().contains("You logged into a secure area!");

        // Store the result as a new row in HTML table format
        String status = loginSuccessful ? "Passed" : "Failed";
        System.out.println(status);
        addTestResult(username,password,status);
    }

    public void addTestResult(String username,String password, String status) {

        String iconHtml;

        if(Objects.equals(status, "Passed")){
            iconHtml = "<img src=\"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgZmlsbD0iIzYzRTZCRSIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBkPSJNMTAgMy41YS41NS41NSAwIDEgMSAxLjEgLjg2TDYuMTYgMTMuNTZhLjU1LjU1IDAgMCAxLS43NyAwTDIuNSA5LjI4YS41NS41NSAwIDAgMSAuNy0uNzFsMy4xMiAzLjEyaDEuOTZ6Ii8+PC9zdmc+\" width=\"16\" height=\"16\">";

        }
        else{
            iconHtml = "<img src=\"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgZmlsbD0iI2ZlNjI2MiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBkPSJNMy41IDMuNWEuNDEuNDEgMCAwIDEgLjU3LS4wOUw4IDcuNDdsMi44LS4yOGEuNDEuNDEgMCAwIDEgLjEwLjcxbC0zLjQgMy40Yy0uMTYuMTUtLjQzLjE1LS41OC4wbC0zLjQtMy40Yy0uMTUuMTUtLjM2LjE0LS41LjAxbC0uMi0uMmMtLjEzLS4xMy0uMTQtLjM2LS4wMS0uNTNsMy41LTMuNXoiLz48L3N2Zz4=\" width=\"16\" height=\"16\">";

        }


        String row = "<tr><td>" + username + "</td><td>" + password + "</td><td>" + status + iconHtml + "</td></tr>";
        testResults.add(row);
    }

    public static void sendEmail(String tableRows) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Load HTML template
            String htmlContent = new String(Files.readAllBytes(Paths.get("D:/Work/things/web/pageforlogin.html")));

            // Replace placeholder with table rows
            htmlContent = htmlContent.replace("{{tableRows}}", tableRows);

            // Prepare email message with HTML content
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO_EMAIL));
            message.setSubject(SUBJECT);
            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            System.out.println("HTML email sent successfully!");

        } catch (MessagingException | IOException e) {
            System.out.println("Failed to send email.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        LoginAutomationWithReport automation = new LoginAutomationWithReport();

        setUp();

        // Read login data from Excel file and attempt login
        automation.readExcelAndLogin("D:/Work/credentials.xlsx");

        tearDown();

        // Combine all rows into a single string and send the email
        String tableRows = String.join("", automation.testResults);
        sendEmail(tableRows);
    }
}
