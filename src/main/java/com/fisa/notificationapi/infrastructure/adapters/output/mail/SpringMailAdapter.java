package com.fisa.notificationapi.infrastructure.adapters.output.mail;

import com.fisa.notificationapi.domain.models.EmailMessage;
import com.fisa.notificationapi.domain.ports.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@RequiredArgsConstructor
public class SpringMailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Async("taskExecutor")
    @Override
    public void sendEmail(EmailMessage message) {
        try {
            log.info("Conectando con servidor SMTP (Mailpit)...");
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("alertasBIAN@fisagrp.com");
            mail.setTo(message.recipient());
            mail.setSubject("[" + message.originService() + "] " + message.subject());
            mail.setText(message.body());

            mailSender.send(mail);
            log.info("Correo enviado a {}", message.recipient());
        } catch (Exception e) {
            log.error("Fallo crítico enviando correo: {}", e.getMessage());
        }
    }
}