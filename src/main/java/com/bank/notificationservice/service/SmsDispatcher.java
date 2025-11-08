package com.bank.notificationservice.service;

import com.bank.notificationservice.config.NotificationProperties;
import com.bank.notificationservice.support.NotificationDeliveryException;
import com.bank.notificationservice.support.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class SmsDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SmsDispatcher.class);

    private final RestClient restClient;
    private final NotificationProperties properties;

    public SmsDispatcher(RestClient.Builder restClientBuilder, NotificationProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.sms().baseUrl())
                .build();
    }

    public void dispatch(SmsMessage message) {
        if (properties.sms().mockDelivery()) {
            log.info("Mock SMS delivery: to={} body='{}'", message.to(), message.body());
            return;
        }

        String sanitizedPhone = sanitize(message.to());
        String path = "/%s/ADDON_SERVICES/SEND/TSMS".formatted(properties.sms().apiKey());
        SmsPayload payload = new SmsPayload(properties.sms().senderId(), sanitizedPhone, message.body());

        try {
            restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Dispatched SMS notification to {}", message.to());
        } catch (RestClientException ex) {
            throw new NotificationDeliveryException("Failed to send notification SMS", ex);
        }
    }

    private String sanitize(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }

    private record SmsPayload(String From, String To, String Msg) {
    }
}
