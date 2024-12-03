package common;

import org.apache.commons.lang3.RandomStringUtils;
import org.jline.reader.LineReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class Utils {
    private static final String CONFIG_FILE = "config.properties";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class.getName());

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return properties;
    }

    public static String generatePaymentReference() {
        return "pay_" + UUID.randomUUID();
    }

    public static String generateOrderId() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    public static String readInput(LineReader reader, String prompt, String defaultValue) {
        var userInput = reader.readLine(prompt);
        if (userInput == null  || userInput.isBlank()) {
            userInput = defaultValue;
        } return userInput;
    }
}
