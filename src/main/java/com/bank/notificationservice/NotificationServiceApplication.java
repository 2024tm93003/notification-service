package com.bank.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificationServiceApplication {

    public static void main(String[] args) {
        // Load env/local.env (if present) and populate System properties so Spring can bind them
        try {
            loadLocalEnv();
        } catch (IOException e) {
            // Log to stderr; do not fail startup because local.env is optional
            System.err.println("Failed to load env/local.env: " + e.getMessage());
        }

        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    private static void loadLocalEnv() throws IOException {
        Path p = Paths.get("env", "local.env");
        if (!Files.exists(p)) {
            return;
        }

        List<String> lines = Files.readAllLines(p);
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            // strip optional surrounding quotes
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            // set raw key as system property as-is (useful for some libraries)
            System.setProperty(key, value);

            String up = key.toUpperCase(Locale.ROOT);
            // Support NOTIFICATION_* keys by mapping to notification.<section>.<property>
            if (up.startsWith("NOTIFICATION_")) {
                String rest = key.substring("NOTIFICATION_".length());
                String[] parts = rest.split("_");
                if (parts.length >= 1) {
                    String section = parts[0].toLowerCase(Locale.ROOT);
                    // build kebab-case for remaining parts
                    if (parts.length > 1) {
                        String[] rem = java.util.Arrays.copyOfRange(parts, 1, parts.length);
                        StringBuilder kebab = new StringBuilder();
                        StringBuilder camel = new StringBuilder();
                        for (int i = 0; i < rem.length; i++) {
                            String seg = rem[i].toLowerCase(Locale.ROOT);
                            if (i > 0) kebab.append('-');
                            kebab.append(seg);

                            if (i == 0) {
                                camel.append(seg);
                            } else {
                                camel.append(Character.toUpperCase(seg.charAt(0))).append(seg.substring(1));
                            }
                        }
                        String propKebab = String.format("notification.%s.%s", section, kebab.toString());
                        String propCamel = String.format("notification.%s.%s", section, camel.toString());
                        System.setProperty(propKebab, value);
                        System.setProperty(propCamel, value);
                    } else {
                        // just notification.<section>=value
                        System.setProperty("notification." + section, value);
                    }
                }
            }

            // Support TWILIO_* keys by mapping to notification.sms.<camel>
            if (up.startsWith("TWILIO_")) {
                String rest = key.substring("TWILIO_".length());
                String[] parts = rest.split("_");
                StringBuilder camel = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String seg = parts[i].toLowerCase(Locale.ROOT);
                    if (i == 0) camel.append(seg);
                    else camel.append(Character.toUpperCase(seg.charAt(0))).append(seg.substring(1));
                }
                String prop = "notification.sms." + camel.toString();
                System.setProperty(prop, value);
            }
        }
    }
}
