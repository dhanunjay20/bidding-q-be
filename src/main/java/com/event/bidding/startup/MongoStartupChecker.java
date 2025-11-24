package com.event.bidding.startup;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

@Component
public class MongoStartupChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoStartupChecker.class);

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Verifying MongoDB connectivity...");
        if (mongoUri == null || mongoUri.isBlank()) {
            log.error("No MongoDB URI configured (spring.data.mongodb.uri). Set SPRING_DATA_MONGODB_URI in your environment or application.properties.");
            throw new IllegalStateException("MongoDB URI not configured");
        }

        // Quick user-friendly pre-check for common localhost case
        try {
            URI uri = new URI(mongoUri);
            String scheme = uri.getScheme();
            if ("mongodb".equalsIgnoreCase(scheme) || "mongodb+srv".equalsIgnoreCase(scheme)) {
                // If host is localhost or 127.0.0.1, try TCP connect test
                String host = uri.getHost();
                int port = uri.getPort();
                if (host != null && (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1"))) {
                    int testPort = (port > 0) ? port : 27017;
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(host, testPort), 3000);
                        log.info("Local MongoDB appears reachable at {}:{}", host, testPort);
                    } catch (Exception e) {
                        log.error("Cannot connect to local MongoDB at {}:{} — Connection refused. Is mongod running?", host, testPort);
                        throw new IllegalStateException("Cannot connect to local MongoDB at " + host + ":" + testPort, e);
                    }
                }
            }
        } catch (Exception e) {
            // ignore URI parsing errors here; we'll attempt a MongoClient ping below which will give better messages
            log.debug("Mongo URI parsing warning: {}", e.getMessage());
        }

        // Attempt a real ping via the MongoDB driver
        try (MongoClient client = MongoClients.create(mongoUri)) {
            Document ping = new Document("ping", 1);
            client.getDatabase("admin").runCommand(ping);
            log.info("Successfully connected to MongoDB (ping OK)");
        } catch (MongoTimeoutException mte) {
            log.error("Timed out while trying to connect to MongoDB. Details: {}", mte.getMessage());
            if (mongoUri.startsWith("mongodb+srv://")) {
                log.error("Your URI uses mongodb+srv. Ensure DNS resolution works and that your network allows outbound connections to MongoDB Atlas (port 27017 and DNS lookups). Also ensure your Atlas IP whitelist allows this client's IP.");
            } else if (mongoUri.contains("localhost") || mongoUri.contains("127.0.0.1")) {
                log.error("Connection refused to localhost. Is the MongoDB server running on this host? Start mongod or update SPRING_DATA_MONGODB_URI to point to a reachable server.");
            }
            throw new IllegalStateException("Failed to connect to MongoDB: " + mte.getMessage(), mte);
        } catch (Exception ex) {
            log.error("Error while connecting to MongoDB: {}", ex.getMessage());
            throw new IllegalStateException("Failed to connect to MongoDB: " + ex.getMessage(), ex);
        }
    }
}

