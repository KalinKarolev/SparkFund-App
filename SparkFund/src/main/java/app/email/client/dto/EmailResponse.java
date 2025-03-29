package app.email.client.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmailResponse {

    private UUID emailId;

    private String userEmail;

    private String subject;

    private String body;

    private LocalDateTime createdOn;
}
