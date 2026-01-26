package com.fisa.notificationapi.application.usecases;

import com.fisa.notificationapi.domain.models.EmailMessage;
import com.fisa.notificationapi.domain.ports.out.EmailPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendEmailUseCaseImplTest {

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private SendEmailUseCaseImpl sut;

    // Fixtures / Builder para reducir duplicación
    private static class EmailMessageBuilder {
        private String recipient = "user@example.com";
        private String subject = "Subject";
        private String body = "Hello body";
        private String originService = "ServiceA";

        EmailMessageBuilder withRecipient(String r) { this.recipient = r; return this; }
        EmailMessageBuilder withSubject(String s) { this.subject = s; return this; }
        EmailMessageBuilder withBody(String b) { this.body = b; return this; }
        EmailMessageBuilder withOriginService(String os) { this.originService = os; return this; }
        EmailMessage build() { return new EmailMessage(recipient, subject, body, originService); }
    }

    private EmailMessage createValidEmailRequest() {
        return new EmailMessageBuilder().build();
    }

    // No aplica SMS en el dominio actual
    @SuppressWarnings("unused")
    private Object createValidSmsRequest() { return null; }

    private EmailMessage createExpectedProviderPayload(EmailMessage base) { return base; }

    @Test
    void send_whenEmailRequestValid_thenCallsEmailClientAndReturnsSuccess() {
        // Given
        EmailMessage message = createValidEmailRequest();
        EmailMessage expected = createExpectedProviderPayload(message);

        // When / Then
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort, times(1)).sendEmail(captor.capture());
        verifyNoMoreInteractions(emailPort);

        EmailMessage sent = captor.getValue();
        assertThat(sent).isSameAs(expected);
        assertThat(sent.recipient()).isEqualTo("user@example.com");
        assertThat(sent.subject()).isEqualTo("Subject");
        assertThat(sent.body()).isEqualTo("Hello body");
        assertThat(sent.originService()).isEqualTo("ServiceA");
    }

    @Test
    void send_whenRecipientMissing_thenDelegatesWithoutThrowing() {
        // Given
        EmailMessage message = new EmailMessageBuilder()
            .withRecipient(null)
            .withSubject("Subject")
            .withBody("Body")
            .withOriginService("ServiceA")
            .build();

        // When / Then
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort).sendEmail(captor.capture());
        verifyNoMoreInteractions(emailPort);

        EmailMessage sent = captor.getValue();
        assertThat(sent.recipient()).isNull();
        assertThat(sent.subject()).isEqualTo("Subject");
        assertThat(sent.body()).isEqualTo("Body");
        assertThat(sent.originService()).isEqualTo("ServiceA");
    }

    @Test
    void send_whenEmailInvalidFormat_thenStillDelegates() {
        // Given
        EmailMessage message = new EmailMessageBuilder()
            .withRecipient("not-an-email")
            .withSubject("Subject")
            .withBody("Body")
            .withOriginService("ServiceA")
            .build();

        // When / Then
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort).sendEmail(captor.capture());
        verifyNoMoreInteractions(emailPort);

        EmailMessage sent = captor.getValue();
        assertThat(sent.recipient()).isEqualTo("not-an-email");
        assertThat(sent.subject()).isEqualTo("Subject");
        assertThat(sent.body()).isEqualTo("Body");
        assertThat(sent.originService()).isEqualTo("ServiceA");
    }

    @Test
    void send_whenOriginServiceNull_thenDelegates() {
        // Given
        EmailMessage message = new EmailMessageBuilder()
            .withOriginService(null)
            .withSubject("Subject")
            .withBody("Body")
            .withRecipient("user@example.com")
            .build();

        // When / Then
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort).sendEmail(captor.capture());
        verifyNoMoreInteractions(emailPort);

        EmailMessage sent = captor.getValue();
        assertThat(sent.originService()).isNull();
    }

    @Test
    void send_whenMessageIsNull_thenThrowsNullPointerException() {
        // Given
        EmailMessage message = null;

        // When / Then
        assertThatThrownBy(() -> sut.send(message))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(emailPort);
    }

    @Test
    void send_whenProviderThrowsRuntimeException_thenPropagates() {
        // Given
        EmailMessage message = createValidEmailRequest();
        doThrow(new RuntimeException("SMTP 5xx")).when(emailPort).sendEmail(any());

        // When / Then
        assertThatThrownBy(() -> sut.send(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SMTP 5xx");
        verify(emailPort).sendEmail(message);
    }

    @Test
    void send_whenProviderThrowsIllegalArgument_thenPropagates() {
        // Given
        EmailMessage message = createValidEmailRequest();
        doThrow(new IllegalArgumentException("bad payload")).when(emailPort).sendEmail(any());

        // When / Then
        assertThatThrownBy(() -> sut.send(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bad payload");
        verify(emailPort).sendEmail(message);
    }

    @Test
    void send_whenLargeSubjectAndBody_thenDelegates() {
        // Given
        String longSubject = "S".repeat(5000);
        String longBody = "B".repeat(10000);
        EmailMessage message = new EmailMessageBuilder()
            .withRecipient("user@example.com")
            .withSubject(longSubject)
            .withBody(longBody)
            .withOriginService("ServiceA")
            .build();

        // When / Then
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort).sendEmail(captor.capture());
        EmailMessage sent = captor.getValue();
        assertThat(sent.subject()).hasSize(5000);
        assertThat(sent.body()).hasSize(10000);
    }

    @Test
    void send_whenCalledTwice_thenCallsPortTwice() {
        // Given
        EmailMessage m1 = createValidEmailRequest();
        EmailMessage m2 = new EmailMessageBuilder()
            .withRecipient("user2@example.com")
            .withSubject("Subject2")
            .withBody("Body2")
            .withOriginService("ServiceB")
            .build();

        // When
        sut.send(m1);
        sut.send(m2);

        // Then
        verify(emailPort, times(1)).sendEmail(m1);
        verify(emailPort, times(1)).sendEmail(m2);
        verifyNoMoreInteractions(emailPort);
    }

    @Test
    void send_whenDelegating_thenPassesSameInstanceToPort() {
        // Given
        EmailMessage message = createValidEmailRequest();

        // When
        sut.send(message);

        // Then
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailPort).sendEmail(captor.capture());
        assertThat(captor.getValue()).isSameAs(message);
    }

    // Parametrizados: variaciones inválidas de email (el caso de uso actual no valida y delega)
    @ParameterizedTest
    @CsvSource({
            "''",             // vacío
            "' '",            // espacios
            "plainaddress",
            "@no-local-part.com",
            "name@domain",  // sin TLD
            "name@domain..com",
            "name@.com"
    })
    void send_whenInvalidEmails_thenStillDelegates(String invalidEmail) {
        // Given
        EmailMessage message = new EmailMessageBuilder()
                .withRecipient(invalidEmail)
                .build();

        // When / Then (sin validación en use case)
        assertThatCode(() -> sut.send(message)).doesNotThrowAnyException();
        verify(emailPort).sendEmail(message);
        verifyNoMoreInteractions(emailPort);
    }

    // No side effects en error de validación: único caso que aplica hoy es mensaje nulo
    @Test
    void send_whenNullMessage_thenNoSideEffects() {
        // Given
        EmailMessage nullMessage = null;

        // When / Then
        assertThatThrownBy(() -> sut.send(nullMessage))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(emailPort);
    }
}
