package com.bank.notificationservice.service;

import com.bank.notificationservice.config.NotificationProperties;
import com.bank.notificationservice.dto.AccountEventNotificationRequest;
import com.bank.notificationservice.dto.AccountStatusChangeNotificationRequest;
import com.bank.notificationservice.dto.HighValueTransactionNotificationRequest;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationComposer composer;
    private final EmailDispatcher dispatcher;
    private final NotificationProperties properties;

    public NotificationService(NotificationComposer composer, EmailDispatcher dispatcher, NotificationProperties properties) {
        this.composer = composer;
        this.dispatcher = dispatcher;
        this.properties = properties;
    }

    public boolean handleHighValueTransaction(HighValueTransactionNotificationRequest request) {
        BigDecimal threshold = resolveThreshold(request);
        if (request.getAmount().compareTo(threshold) < 0) {
            log.info("Skipping high value alert for transaction below threshold: amount={} threshold={} account={}",
                    request.getAmount(), threshold, request.getAccountNumber());
            return false;
        }
        dispatcher.dispatch(composer.composeHighValueTransaction(request, threshold));
        return true;
    }

    public void handleAccountStatusChange(AccountStatusChangeNotificationRequest request) {
        dispatcher.dispatch(composer.composeAccountStatusChange(request));
    }

    public void handleAccountEvent(AccountEventNotificationRequest request) {
        dispatcher.dispatch(composer.composeAccountEvent(request));
    }

    private BigDecimal resolveThreshold(HighValueTransactionNotificationRequest request) {
        if (request.getThresholdOverride() != null && request.getThresholdOverride().signum() > 0) {
            return request.getThresholdOverride();
        }
        return properties.getThresholds().getHighValueTransaction();
    }
}

