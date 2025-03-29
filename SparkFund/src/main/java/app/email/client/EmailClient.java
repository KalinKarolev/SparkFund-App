package app.email.client;

import app.email.client.dto.EmailRequest;
import app.email.client.dto.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "sparkmail-svc", url = "http://localhost:8081/api/v1/emails")
public interface EmailClient {

    @PostMapping()
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest emailRequest);

    @GetMapping
    ResponseEntity<List<EmailResponse>> getFailedEmails();

    @DeleteMapping("/failed")
    void deletedFailedEmail(@RequestParam(name ="emailId") UUID emailId);
}
