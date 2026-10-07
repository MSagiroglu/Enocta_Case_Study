package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Konfigurasyon okuyucu. Oncelik sirasi:
 * 1) JVM system property  (mvn verify -Dbrowser=firefox)
 * 2) Ortam degiskeni       (BROWSER=firefox, MOCK_SERVER_URL=..., nokta yerine alt cizgi)
 * 3) configuration.properties
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    private static Properties load() {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("configuration.properties")) {
            if (in == null) {
                throw new IllegalStateException("configuration.properties classpath'te bulunamadi.");
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("configuration.properties okunamadi.", e);
        }
    }

    public static String getProperty(String key) {
        String systemValue = System.getProperty(key);
        if (isNotBlank(systemValue)) {
            return systemValue.trim();
        }
        String envValue = System.getenv(key.toUpperCase(Locale.ENGLISH).replace('.', '_'));
        if (isNotBlank(envValue)) {
            return envValue.trim();
        }
        return PROPERTIES.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return isNotBlank(value) ? value : defaultValue;
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key, "false"));
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
