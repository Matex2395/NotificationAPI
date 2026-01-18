package com.fisa.notificationapi.domain.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {
    private String recipient;
    private String subject;
    private String body;
    private String originService; // Para trazabilidad (Mapper, Party Service Operation, ValidationAPI, etc.)
}