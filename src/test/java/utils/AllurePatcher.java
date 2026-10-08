package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

public class AllurePatcher {
    public static void main(String[] args) {
        try {
            File dir = new File("target/allure-results");
            if (!dir.exists()) return;

            ObjectMapper mapper = new ObjectMapper();

            for (File file : dir.listFiles((d, name) -> name.endsWith("-result.json"))) {
                JsonNode root = mapper.readTree(file);
                if (root.isObject()) {
                    ObjectNode node = (ObjectNode) root;
                    String browser = "UNKNOWN";

                    String content = node.toString();
                    if (content.contains("Token Alma") || content.contains("API Mock")) {
                        browser = "API";
                    } else if (content.contains("TestNG-tests-1")) {
                        browser = "CHROME";
                    } else if (content.contains("TestNG-tests-2")) {
                        browser = "FIREFOX";
                    } else if (content.contains("TestNG-tests-3")) {
                        browser = "EDGE";
                    }

                    if (!browser.equals("UNKNOWN")) {
                        if (node.has("historyId")) {
                            node.put("historyId", node.get("historyId").asText() + "-" + browser);
                        }
                        if (node.has("name")) {
                            node.put("name", node.get("name").asText() + " [" + browser + "]");
                        }
                        
                        mapper.writeValue(file, node);
                        System.out.println("Patched Allure result: " + file.getName() + " for " + browser);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
