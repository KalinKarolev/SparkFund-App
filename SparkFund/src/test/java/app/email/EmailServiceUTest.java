package app.email;

import app.email.client.EmailClient;
import app.email.client.dto.EmailRequest;
import app.email.client.dto.EmailResponse;
import app.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUTest {

    @Mock
    private EmailClient emailClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    void givenValidEmailRequest_whenSendEmail_thenReturnHttpStatus() {
        EmailRequest emailRequest = EmailRequest.builder()
                .userEmail("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(emailClient.sendEmail(any(EmailRequest.class))).thenReturn(mockResponse);

        HttpStatusCode statusCode = emailService.sendEmail("user@example.com", "Test Subject", "Test Body");

        assertEquals(HttpStatus.OK, statusCode);
        verify(emailClient, times(1)).sendEmail(emailRequest);
    }

    @Test
    void givenFailedEmailResponse_whenSendEmail_thenLogError() {
        EmailRequest emailRequest = EmailRequest.builder()
                .userEmail("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(emailClient.sendEmail(any(EmailRequest.class))).thenReturn(mockResponse);

        HttpStatusCode statusCode = emailService.sendEmail("user@example.com", "Test Subject", "Test Body");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);
        verify(emailClient, times(1)).sendEmail(emailRequest);
    }

    @Test
    void givenNullUserEmail_whenSendEmail_thenLogError() {
        HttpStatusCode result = emailService.sendEmail(null, "Test Subject", "Test Body");

        assertEquals(HttpStatus.BAD_REQUEST, result);
        verify(emailClient, never()).sendEmail(any(EmailRequest.class));
    }

    @Test
    void givenFailedEmailsExist_whenGetFailedEmails_thenReturnList() {
        EmailResponse failedEmail = EmailResponse.builder()
                .emailId(UUID.randomUUID())
                .userEmail("user@example.com")
                .subject("Failed Email")
                .body("Body")
                .build();

        ResponseEntity<List<EmailResponse>> responseEntity = new ResponseEntity<>(List.of(failedEmail), HttpStatus.OK);
        when(emailClient.getFailedEmails()).thenReturn(responseEntity);

        List<EmailResponse> result = emailService.getFailedEmails();

        assertEquals(1, result.size());
        assertEquals(failedEmail.getUserEmail(), result.get(0).getUserEmail());
        verify(emailClient, times(1)).getFailedEmails();
    }

    @Test
    void givenNullResponse_whenGetFailedEmails_thenReturnEmptyList() {
        when(emailClient.getFailedEmails()).thenReturn(null);

        List<EmailResponse> result = emailService.getFailedEmails();

        assertTrue(result.isEmpty());
        verify(emailClient, times(1)).getFailedEmails();
    }

    @Test
    void givenEmailId_whenDeletedFailedEmail_thenCallClient() {
        UUID emailId = UUID.randomUUID();
        emailService.deletedFailedEmail(emailId);
        verify(emailClient, times(1)).deletedFailedEmail(emailId);
    }
}
