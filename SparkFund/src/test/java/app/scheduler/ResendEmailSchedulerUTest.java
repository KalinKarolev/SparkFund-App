package app.scheduler;

import app.email.client.dto.EmailResponse;
import app.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResendEmailSchedulerUTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ResendEmailScheduler resendEmailScheduler;

    @Test
    void givenFailedEmails_whenResendEmails_thenResendAndDeleteSuccessfulOnes() {
        EmailResponse failedEmail1 = EmailResponse.builder()
                .emailId(UUID.randomUUID())
                .userEmail("user1@example.com")
                .subject("Test Subject 1")
                .body("Test Body 1")
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();

        EmailResponse failedEmail2 = EmailResponse.builder()
                .emailId(UUID.randomUUID())
                .userEmail("user2@example.com")
                .subject("Test Subject 2")
                .body("Test Body 2")
                .createdOn(LocalDateTime.now().minusDays(2))
                .build();

        List<EmailResponse> failedEmails = List.of(failedEmail1, failedEmail2);

        when(emailService.getFailedEmails()).thenReturn(failedEmails);
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(HttpStatus.OK);

        resendEmailScheduler.resendEmails();

        verify(emailService, times(1)).getFailedEmails();
        verify(emailService, times(1)).sendEmail(failedEmail1.getUserEmail(), failedEmail1.getSubject(), failedEmail1.getBody());
        verify(emailService, times(1)).sendEmail(failedEmail2.getUserEmail(), failedEmail2.getSubject(), failedEmail2.getBody());
        verify(emailService, times(1)).deletedFailedEmail(failedEmail1.getEmailId());
        verify(emailService, times(1)).deletedFailedEmail(failedEmail2.getEmailId());
    }

    @Test
    void givenNoFailedEmails_whenResendEmails_thenDoNothing() {
        when(emailService.getFailedEmails()).thenReturn(Collections.emptyList());

        resendEmailScheduler.resendEmails();

        verify(emailService, times(1)).getFailedEmails();
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).deletedFailedEmail(any());
    }

    @Test
    void givenFailedEmails_whenResendFails_thenDoNotDeleteEmail() {
        EmailResponse failedEmail = EmailResponse.builder()
                .emailId(UUID.randomUUID())
                .userEmail("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();

        when(emailService.getFailedEmails()).thenReturn(List.of(failedEmail));
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        resendEmailScheduler.resendEmails();

        verify(emailService, times(1)).getFailedEmails();
        verify(emailService, times(1)).sendEmail(failedEmail.getUserEmail(), failedEmail.getSubject(), failedEmail.getBody());
        verify(emailService, never()).deletedFailedEmail(any());
    }
}
