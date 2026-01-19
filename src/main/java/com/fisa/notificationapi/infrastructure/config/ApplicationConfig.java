package com.fisa.notificationapi.infrastructure.config;

import com.fisa.notificationapi.application.usecases.SendEmailUseCaseImpl;
import com.fisa.notificationapi.domain.ports.in.SendEmailUseCase;
import com.fisa.notificationapi.domain.ports.out.EmailPort;
import com.fisa.notificationapi.infrastructure.adapters.output.mail.SpringMailAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class ApplicationConfig {

    @Bean
    public EmailPort emailPort(JavaMailSender javaMailSender) {
        return new SpringMailAdapter(javaMailSender);
    }

    @Bean
    public SendEmailUseCase sendEmailUseCase(EmailPort emailPort) {
        return new SendEmailUseCaseImpl(emailPort);
    }
}