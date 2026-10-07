package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtils {
    private static PrintWriter fileWriter;
    
    static {
        try {
            File logDir = new File("target/logs");
            if (!logDir.exists()) logDir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            fileWriter = new PrintWriter(new FileWriter("target/logs/TestRun_" + timestamp + ".log", true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }

    private static synchronized void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String threadId = "Worker-" + Thread.currentThread().getId();
        String browser = ConfigReader.getProperty("browser").toUpperCase(); // Default
        
        // If Driver has a thread local browser name (cross-browser run), use it
        if (Driver.getBrowserName() != null) {
            browser = Driver.getBrowserName().toUpperCase();
        }

        String logEntry = String.format("[%s] [%s] [%s] [%s] - %s", timestamp, level, threadId, browser, message);
        
        // Print to console with color if INFO (ANSI escape code)
        if (level.equals("ERROR")) {
            System.err.println(logEntry);
        } else {
            // Cyan color for console
            System.out.println("\u001B[36m" + logEntry + "\u001B[0m");
        }
        
        // Write to file
        if (fileWriter != null) {
            fileWriter.println(logEntry);
        }
    }
}
