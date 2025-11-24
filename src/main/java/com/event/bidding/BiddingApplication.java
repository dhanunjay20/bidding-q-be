package com.event.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class BiddingApplication {

    public static void main(String[] args) {
        // Load .env file into System properties (development convenience).
        // This does not override existing System properties or environment variables.
        try {
            loadDotEnvIfPresent();
        } catch (Exception e) {
            System.err.println("Warning: failed to load .env file: " + e.getMessage());
        }

        // If no Mongo URI provided and a dev profile file exists, enable 'dev' profile for IDE runs
        try {
            ensureDevProfileIfAvailable();
        } catch (Exception e) {
            System.err.println("Warning: failed to set dev profile: " + e.getMessage());
        }

        SpringApplication.run(BiddingApplication.class, args);
    }

    private static void loadDotEnvIfPresent() throws IOException {
        Path env = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(env)) return;
        List<String> lines = Files.readAllLines(env, StandardCharsets.UTF_8);
        for (String raw : lines) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            // don't overwrite existing System properties or environment variables
            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
                System.out.println("Loaded .env -> System property: " + key);
            }
        }
    }

    private static void ensureDevProfileIfAvailable() {
        // prefer explicit system property or env var
        String mongoEnv = System.getProperty("SPRING_DATA_MONGODB_URI");
        if (mongoEnv == null || mongoEnv.isBlank()) {
            mongoEnv = System.getenv("SPRING_DATA_MONGODB_URI");
        }
        if (mongoEnv == null || mongoEnv.isBlank()) {
            // check for application-dev.properties in classpath (src/main/resources)
            Path dev = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "application-dev.properties");
            if (Files.exists(dev)) {
                // Only set profile if not already set
                if (System.getProperty("spring.profiles.active") == null && System.getenv("SPRING_PROFILES_ACTIVE") == null) {
                    System.setProperty("spring.profiles.active", "dev");
                    System.out.println("Auto-enabled 'dev' profile because application-dev.properties exists and no SPRING_DATA_MONGODB_URI was provided.");
                }
            }
        }
    }

}
