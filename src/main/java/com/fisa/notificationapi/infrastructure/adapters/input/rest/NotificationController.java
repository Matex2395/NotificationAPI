package com.fisa.notificationapi.infrastructure.adapters.input.rest;

import com.fisa.notificationapi.domain.models.EmailMessage;
import com.fisa.notificationapi.domain.ports.in.SendEmailUseCase;
import com.fisa.notificationapi.infrastructure.adapters.input.rest.dtos.NotificationRequest;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SendEmailUseCase sendEmailUseCase;

    @PostMapping("/email")
    @RateLimiter(name = "emailService", fallbackMethod = "fallbackRateLimit")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {

        EmailMessage domainMessage = new EmailMessage(
                request.getRecipient(),
                request.getSubject(),
                request.getBody(),
                request.getOriginService()
        );

        sendEmailUseCase.send(domainMessage);

        return ResponseEntity.accepted().body("Notificación encolada exitosamente.");
    }

    public ResponseEntity<String> fallbackRateLimit(NotificationRequest request, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("El sistema de notificaciones está saturado. Intente más tarde.");
    }
}