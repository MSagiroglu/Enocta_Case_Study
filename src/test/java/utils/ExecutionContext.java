package utils;

import java.util.Locale;

/**
 * Calisan thread'in hangi hedefe (chrome / firefox / edge / api) ait oldugunu tutar.
 * Runner'lar @BeforeTest'te set eder; Driver, LoggerUtils, Hooks ve raporlayici buradan okur.
 * InheritableThreadLocal sayesinde Cucumber'in actigi alt thread'ler de ayni degeri gorur.
 */
public final class ExecutionContext {

    private static final InheritableThreadLocal<String> TARGET = new InheritableThreadLocal<>();

    private ExecutionContext() {
    }

    public static void setTarget(String target) {
        TARGET.set(target == null ? null : target.trim().toLowerCase(Locale.ENGLISH));
    }

    /** Ornek: "chrome", "firefox", "edge", "api". Set edilmemisse config'teki tarayici. */
    public static String getTarget() {
        String target = TARGET.get();
        return target != null ? target : ConfigReader.getProperty("browser", "chrome").toLowerCase(Locale.ENGLISH);
    }

    /** Log ve raporlarda kullanilan buyuk harfli etiket. Ornek: "CHROME". */
    public static String getLabel() {
        return getTarget().toUpperCase(Locale.ENGLISH);
    }
}
