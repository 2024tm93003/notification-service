package com.bank.notificationservice.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private final Mail mail = new Mail();
    private final Thresholds thresholds = new Thresholds();

    public Mail getMail() {
        return mail;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public static class Mail {

        @Email
        @NotBlank
        private String from = "noreply@bank.example";

        private boolean mockDelivery = true;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public boolean isMockDelivery() {
            return mockDelivery;
        }

        public void setMockDelivery(boolean mockDelivery) {
            this.mockDelivery = mockDelivery;
        }
    }

    public static class Thresholds {

        @Positive
        private BigDecimal highValueTransaction = BigDecimal.valueOf(10_000L);

        public BigDecimal getHighValueTransaction() {
            return highValueTransaction;
        }

        public void setHighValueTransaction(BigDecimal highValueTransaction) {
            this.highValueTransaction = highValueTransaction;
        }
    }
}

