package app.scheduler;

import app.email.client.dto.EmailResponse;
import app.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ResendEmailScheduler {

    private final EmailService emailService;

    public ResendEmailScheduler(EmailService _emailService) {
        emailService = _emailService;
    }

    @Scheduled(fixedRate = 10000)
//    @Scheduled(fixedRate = 1800000)
    public void resendEmails() {
        List<EmailResponse> failedEmails = emailService.getFailedEmails();
        if (failedEmails.isEmpty()) {
            log.info("No emails found for resending");
        }
        for (EmailResponse failedEmail : failedEmails) {
            HttpStatusCode statusCode = emailService.sendEmail(failedEmail.getUserEmail(), failedEmail.getSubject(), failedEmail.getBody());
            if (statusCode.is2xxSuccessful()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy");
                String formattedDate = failedEmail.getCreatedOn().format(formatter);
                log.info("Email to {}, created on {}, was sent successfully from scheduler", failedEmail.getUserEmail(), formattedDate);
                emailService.deletedFailedEmail(failedEmail.getEmailId());
            }
        }
    }
}
