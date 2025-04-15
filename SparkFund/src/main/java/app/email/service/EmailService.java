package app.email.service;

import app.email.client.EmailClient;
import app.email.client.dto.EmailRequest;
import app.email.client.dto.EmailResponse;
import app.web.dto.EmailEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final EmailClient emailClient;

    public EmailService(EmailClient _emailClient) {
        emailClient = _emailClient;
    }

    public HttpStatusCode sendEmail(String userEmail, String emailSubject, String emailBody) {
        if (userEmail == null) {
            log.warn("[Feign call to sparkmail-svc failed] User email is missing");
            return HttpStatus.BAD_REQUEST;
        }
        EmailRequest emailRequest = EmailRequest.builder()
                .userEmail(userEmail)
                .subject(emailSubject)
                .body(emailBody)
                .build();
        ResponseEntity<Void> httpResponse = emailClient.sendEmail(emailRequest);
        if (httpResponse == null) {
            log.error("[Feign call to sparkmail-svc failed] Response is null");
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to sparkmail-svc failed with code: {}] Can`t send email to {}", httpResponse.getStatusCode(), userEmail);
        }
        return httpResponse.getStatusCode();
    }
    
    public List<EmailResponse> getFailedEmails() {
        ResponseEntity<List<EmailResponse>> responseResult = emailClient.getFailedEmails();
        if (responseResult == null) {
            return new ArrayList<>();
        }
        return responseResult.getBody();
    }

    public void deletedFailedEmail(UUID emailId) {
        emailClient.deletedFailedEmail(emailId);
    }

    @Async
    @EventListener
    public void sendEmailForClosedSignal(EmailEvent event) {
        String subject = "Your signal has been resolved";
        String emailBody = "Your user signal titled \"" + event.getSignalTitle() + "\" has been reviewed and closed by our team.\n\n" +
                "Admin Response:\n" +
                "\"" + event.getAdminResponse() + "\"\n\n" +
                "If you have any further questions or concerns, feel free to reach out to us.\n\n" +
                "Best regards,\n" +
                "The SparkFund Team";
        sendEmail(event.getUserEmail(), subject, emailBody);
    }
}
