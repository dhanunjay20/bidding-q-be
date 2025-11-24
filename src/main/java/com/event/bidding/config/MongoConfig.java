package com.event.bidding.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${SPRING_DATA_MONGODB_URI:}")
    private String mongoUriEnv;

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/eventbidding}")
    private String mongoUriDefault;

    // How long (ms) the driver should wait to select a server before giving up on startup checks
    @Value("${SPRING_DATA_MONGODB_SERVER_SELECTION_TIMEOUT_MS:5000}")
    private int serverSelectionTimeoutMs;

    // Connect timeout for socket (ms)
    @Value("${SPRING_DATA_MONGODB_SOCKET_CONNECT_TIMEOUT_MS:2000}")
    private int socketConnectTimeoutMs;

    // If true, a failed ping on startup will cause the application context to fail. Set false to allow app to start in degraded mode.
    @Value("${app.mongodb.failOnStartup:false}")
    private boolean failOnStartup;

    // If true, perform a lightweight ping during startup to verify connectivity. Default false to avoid blocking startup.
    @Value("${app.mongodb.pingOnStartup:false}")
    private boolean pingOnStartup;

    @Bean
    public MongoClient mongoClient() {
        String connectionString = determineConnectionString();
        log.info("Using MongoDB connection string: {}", maskConnectionString(connectionString));
        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .applyToClusterSettings(b -> b.serverSelectionTimeout(serverSelectionTimeoutMs, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(b -> b.connectTimeout(socketConnectTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        MongoClient client = MongoClients.create(settings);

        // Optional startup ping: only run if explicitly enabled to avoid blocking startup in environments
        // where DNS or network can be slow (Cloud Run, etc.). Configure via app.mongodb.pingOnStartup=true
        if (pingOnStartup) {
            try {
                log.info("Pinging MongoDB to verify connectivity (timeout {} ms)...", serverSelectionTimeoutMs);
                Document ping = new Document("ping", 1);
                client.getDatabase("admin").runCommand(ping);
                log.info("Successfully connected to MongoDB (ping OK)");
            } catch (Exception e) {
                log.warn("Unable to ping MongoDB during startup: {}", e.toString());
                if (failOnStartup) {
                    log.error("app.mongodb.failOnStartup is true — failing application startup due to Mongo connectivity");
                    try { client.close(); } catch (Exception ignore) {}
                    throw new RuntimeException("Failed to connect to MongoDB during startup", e);
                } else {
                    log.warn("Continuing startup in degraded mode; Mongo operations will retry when first used.");
                }
            }
        } else {
            log.debug("app.mongodb.pingOnStartup is false — skipping startup ping to avoid blocking startup.");
        }

        return client;
    }

    private String determineConnectionString() {
        if (mongoUriEnv != null && !mongoUriEnv.isBlank()) {
            String candidate = mongoUriEnv.trim();
            if (isValidPrefix(candidate)) {
                return candidate;
            } else {
                log.warn("Environment Mongo URI does not start with 'mongodb://' or 'mongodb+srv://', ignoring and falling back to default. Value: {}", maskConnectionString(candidate));
            }
        }
        // Use default from application.properties (which itself has fallback)
        if (mongoUriDefault != null && !mongoUriDefault.isBlank()) {
            if (isValidPrefix(mongoUriDefault.trim())) {
                return mongoUriDefault.trim();
            }
        }
        // Absolute fallback
        return "mongodb://localhost:27017/eventbidding";
    }

    private boolean isValidPrefix(String uri) {
        String lower = uri.toLowerCase();
        return lower.startsWith("mongodb://") || lower.startsWith("mongodb+srv://");
    }

    private String maskConnectionString(String uri) {
        if (uri == null) return "(null)";
        // Do not reveal passwords
        try {
            String s = uri;
            int at = s.indexOf('@');
            if (at > 0) {
                int schemeEnd = s.indexOf("://");
                if (schemeEnd >= 0 && schemeEnd < s.length()) {
                    String prefix = s.substring(0, schemeEnd + 3);
                    String suffix = s.substring(at + 1);
                    return prefix + "***@" + suffix;
                }
            }
            return uri;
        } catch (Exception e) {
            return "(unparsable)";
        }
    }
}
