package com.bank.notificationservice.dto;

import com.bank.notificationservice.model.AccountEventType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AccountEventNotificationRequest {

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String customerName;

    @Email
    @NotBlank
    private String customerEmail;

    @NotNull
    private AccountEventType eventType;

    private String description;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public AccountEventType getEventType() {
        return eventType;
    }

    public void setEventType(AccountEventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
