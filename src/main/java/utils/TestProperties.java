package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to load, get, set and update the test properties
 * @author Nayeem John
 */
public class TestProperties {

    private Logger log = LogManager.getLogger();
    private Properties prop;

    /**
     * Constructor - Load the config properties for Test
     */
    public TestProperties() {
        this.prop = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("./src/main/resources/config.properties")) {
            prop.load(fileInputStream);
        } catch (IOException e) {
            log.error("Error while reading properties file ", e);
        }
    }

    /**
     * Utility method to get the property value after trim
     * 
     * @param key - {@link String} - key of the property
     * @return String - Returns value {@link String} of the property
     */
    public String getProperty(String key) {
        return prop.getProperty(key) != null ? prop.getProperty(key).trim() : null;
    }

    /**
     * Method to set the property with the value
     * 
     * @param key   - {@link String} - key of the property
     * @param value - {@link String} - value of the property
     */
    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }

    /**
     * Method to update the Test properties with Run time System Property
     * 
     */
    public void updateTestProperties() {
        prop.keySet().forEach(key -> {
            String propKey = (String) key;
            if (System.getProperty(propKey) != null)
                prop.setProperty(propKey, System.getProperty(propKey));
        });
    }
}
