package com.fisa.notificationapi.domain.ports.out;

import com.fisa.notificationapi.domain.models.EmailMessage;

public interface EmailPort {
    void sendEmail(EmailMessage message);
}