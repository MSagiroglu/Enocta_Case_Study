package utils;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            FileInputStream file = new FileInputStream("src/test/resources/configuration.properties");
            properties = new Properties();
            properties.load(file);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String sysProp = System.getenv(key.toUpperCase().replace(".", "_"));
        if (sysProp != null) return sysProp;
        return properties.getProperty(key);
    }
}
