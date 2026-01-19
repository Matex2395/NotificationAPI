package com.fisa.notificationapi.application.usecases;

import com.fisa.notificationapi.domain.models.EmailMessage;
import com.fisa.notificationapi.domain.ports.in.SendEmailUseCase;
import com.fisa.notificationapi.domain.ports.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SendEmailUseCaseImpl implements SendEmailUseCase {

    private final EmailPort emailPort;

    @Override
    public void send(EmailMessage message) {
        log.info("Procesando solicitud de correo desde: {}", message.originService());
        emailPort.sendEmail(message);
    }
}