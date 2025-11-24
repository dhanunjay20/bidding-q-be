package com.event.bidding.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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

    @Bean
    public MongoClient mongoClient() {
        String connectionString = determineConnectionString();
        log.info("Using MongoDB connection string: {}", maskConnectionString(connectionString));
        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();
        return MongoClients.create(settings);
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

