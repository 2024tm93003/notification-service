package com.bank.notificationservice.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(Mail mail, Thresholds thresholds) {

    private static final String DEFAULT_FROM_ADDRESS = "noreply@bank.example";
    private static final boolean DEFAULT_MOCK_DELIVERY = true;
    private static final BigDecimal DEFAULT_THRESHOLD_AMOUNT = BigDecimal.valueOf(10_000L);
    private static final Mail DEFAULT_MAIL = new Mail(DEFAULT_FROM_ADDRESS, DEFAULT_MOCK_DELIVERY);
    private static final Thresholds DEFAULT_THRESHOLDS = new Thresholds(DEFAULT_THRESHOLD_AMOUNT);

    public NotificationProperties {
        mail = mail != null ? mail : DEFAULT_MAIL;
        thresholds = thresholds != null ? thresholds : DEFAULT_THRESHOLDS;
    }

    public static record Mail(
            @Email @NotBlank String from,
            @DefaultValue("true") boolean mockDelivery) {

        public Mail {
            from = from != null ? from : DEFAULT_FROM_ADDRESS;
        }
    }

    public static record Thresholds(@Positive BigDecimal highValueTransaction) {

        public Thresholds {
            highValueTransaction = highValueTransaction != null ? highValueTransaction : DEFAULT_THRESHOLD_AMOUNT;
        }
    }
}
