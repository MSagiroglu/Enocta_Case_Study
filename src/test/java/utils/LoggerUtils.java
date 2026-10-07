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
    
    public static void warning(String message) {
        log("WARNING", message);
    }

    private static final ConcurrentHashMap<Long, Integer> threadMap = new ConcurrentHashMap<>();
    private static final AtomicInteger threadCounter = new AtomicInteger(1);

    private static synchronized void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long tId = Thread.currentThread().getId();
        int workerId = threadMap.computeIfAbsent(tId, k -> threadCounter.getAndIncrement());
        String threadId = "Worker-" + workerId;
        String browser = ConfigReader.getProperty("browser").toUpperCase(Locale.ENGLISH); // Default
        
        // If Driver has a thread local browser name (cross-browser run), use it
        if (Driver.getBrowserName() != null) {
            browser = Driver.getBrowserName().toUpperCase(Locale.ENGLISH);
        }

        // Determine Tag instead of Emoji for cross-platform safety
        String emoji = "[INFO]";
        if (level.equals("ERROR")) emoji = "[FAIL]";
        else if (level.equals("SUCCESS")) emoji = "[ OK ]";

        // Determine color
        String color = "\u001B[36m"; // Default Cyan
        if (level.equals("ERROR")) {
            color = "\u001B[31m"; // Red
        } else if (browser.contains("CHROME")) {
            color = "\u001B[33m"; // Yellow
        } else if (browser.contains("FIREFOX")) {
            color = "\u001B[35m"; // Purple
        } else if (browser.contains("EDGE")) {
            color = "\u001B[34m"; // Blue
        } else {
            color = "\u001B[32m"; // Green for API
        }

        String logEntryConsole = String.format("%s%s [%s] [%s] [%s] - %s\u001B[0m", color, emoji, timestamp, threadId, browser, message);
        String logEntryFile = String.format("[%s] [%s] [%s] [%s] - %s", timestamp, level, threadId, browser, message);
        
        System.out.println(logEntryConsole);
        
        // Write to file
        if (fileWriter != null) {
            fileWriter.println(logEntryFile);
        }
    }
    
    public static void success(String message) {
        log("SUCCESS", message);
    }
}
