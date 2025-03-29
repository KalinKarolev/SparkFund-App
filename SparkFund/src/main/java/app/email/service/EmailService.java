package app.email.service;

import app.email.client.EmailClient;
import app.email.client.dto.EmailRequest;
import app.email.client.dto.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
        }
        EmailRequest emailRequest = EmailRequest.builder()
                .userEmail(userEmail)
                .subject(emailSubject)
                .body(emailBody)
                .build();
        ResponseEntity<Void> httpResponse = emailClient.sendEmail(emailRequest);
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
}
