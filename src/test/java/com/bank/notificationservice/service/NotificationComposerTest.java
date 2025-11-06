package com.bank.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.notificationservice.dto.AccountEventNotificationRequest;
import com.bank.notificationservice.dto.AccountStatusChangeNotificationRequest;
import com.bank.notificationservice.dto.HighValueTransactionNotificationRequest;
import com.bank.notificationservice.model.AccountEventType;
import com.bank.notificationservice.support.EmailMessage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationComposerTest {

    private NotificationComposer composer;

    @BeforeEach
    void setUp() {
        composer = new NotificationComposer();
    }

    @Test
    void composeHighValueTransaction_shouldRenderExpectedBody() {
        HighValueTransactionNotificationRequest request = new HighValueTransactionNotificationRequest();
        request.setAccountNumber("1234567890");
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");
        request.setTxnType("DEBIT");
        request.setAmount(BigDecimal.valueOf(15000));
        request.setCurrency("USD");
        request.setCounterparty("ACME Corp");
        request.setReference("Invoice 42");

        EmailMessage message = composer.composeHighValueTransaction(request, BigDecimal.valueOf(10000));

        assertThat(message.to()).isEqualTo("jane@example.com");
        assertThat(message.subject()).contains("High value DEBIT alert");
        assertThat(message.body()).contains("Jane Doe");
        assertThat(message.body()).contains("ACME Corp");
        assertThat(message.body()).contains("Invoice 42");
    }

    @Test
    void composeAccountStatusChange_shouldIncludeStatusDetails() {
        AccountStatusChangeNotificationRequest request = new AccountStatusChangeNotificationRequest();
        request.setAccountNumber("9876543210");
        request.setCustomerName("John Smith");
        request.setCustomerEmail("john@example.com");
        request.setPreviousStatus("Pending KYC");
        request.setCurrentStatus("Active");
        request.setRemarks("KYC documents verified");

        EmailMessage message = composer.composeAccountStatusChange(request);

        assertThat(message.subject()).contains("Account status updated");
        assertThat(message.body()).contains("Pending KYC");
        assertThat(message.body()).contains("Active");
        assertThat(message.body()).contains("KYC documents verified");
    }

    @Test
    void composeAccountEvent_shouldFormatEventSpecificMessage() {
        AccountEventNotificationRequest request = new AccountEventNotificationRequest();
        request.setAccountNumber("222333444");
        request.setCustomerName("Mary Major");
        request.setCustomerEmail("mary@example.com");
        request.setEventType(AccountEventType.LOAN_CLEARED);
        request.setDescription("Final EMI received on 2024-02-20");

        EmailMessage message = composer.composeAccountEvent(request);

        assertThat(message.subject()).contains("loan is closed");
        assertThat(message.body()).contains("Final EMI received");
        assertThat(message.body()).contains("Mary Major");
    }
}

