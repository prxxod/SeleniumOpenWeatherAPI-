import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.restassured.response.Response;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import io.restassured.path.json.JsonPath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SeleniumWithAPIExample {

    private WebDriver driver;

    private static final String FROM_EMAIL = "pramodaskdrive01@gmail.com";
    private static final String PASSWORD = "iyzxadvqmtuihehu";
    private static final String TO_EMAIL = "pramodask2003@gmail.com";
    private static final String SUBJECT = "API Test Result";

    private static final String API_KEY = "d47722be309f1589b592b7d3042acf5e";

    private List<String> testResults = new ArrayList<>();

    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "D:/Work/things/chromedriver-win64/chromedriver.exe");
        driver = new ChromeDriver();
    }

    public void addTestResult(String testName, String status, String dataReceived) {
        String row = "<tr><td>" + testName + "</td><td>" + status + "</td><td>" + dataReceived + "</td></tr>";
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
            String htmlContent = new String(Files.readAllBytes(Paths.get("D:/Work/things/web/pageforweather.html")));

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

    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public void getWeatherFromAPI(String testName,String city_name) {
        Response response = io.restassured.RestAssured.given()
                .queryParam("q", city_name)
                .queryParam("appid", API_KEY)
                .queryParam("units", "metric")
                .get("http://api.openweathermap.org/data/2.5/weather");
        JsonPath jsonPath = response.jsonPath();

        // Extract temperature and humidity
        String temperature = jsonPath.getString("main.temp");
        String humidity = jsonPath.getString("main.humidity");

        // Build a formatted string with extracted data
        String weatherData = "Location: " + city_name + "\n" + "Temperature: " + temperature + "°C, Humidity: " + humidity + "%";

//        String weatherData = "Location: " + city_name + "  " + response.asString();
        String status = (response.getStatusCode() == 200) ? "Passed" : "Failed";
        addTestResult(testName, status, weatherData);
    }

    public static void main(String[] args) {
        SeleniumWithAPIExample example = new SeleniumWithAPIExample();

        example.setUp();

        // Running multiple test cases
        example.getWeatherFromAPI("Weather Test Case 1","London");
        example.getWeatherFromAPI("Weather Test Case 2","Kovvur");

        example.tearDown();

        // Combine all rows into a single string and send the email
        String tableRows = String.join("", example.testResults);
        sendEmail(tableRows);
    }
}
