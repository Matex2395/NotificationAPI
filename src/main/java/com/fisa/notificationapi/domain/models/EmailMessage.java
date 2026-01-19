package com.fisa.notificationapi.domain.models;

public record EmailMessage(
        String recipient,
        String subject,
        String body,
        String originService
) {}