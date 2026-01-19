package com.fisa.notificationapi.domain.ports.in;

import com.fisa.notificationapi.domain.models.EmailMessage;

public interface SendEmailUseCase {
    void send(EmailMessage message);
}