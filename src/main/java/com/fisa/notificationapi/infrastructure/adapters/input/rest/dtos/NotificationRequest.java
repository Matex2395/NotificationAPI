package com.fisa.notificationapi.infrastructure.adapters.input.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String recipient;
    private String subject;
    private String body;
    private String originService;
}