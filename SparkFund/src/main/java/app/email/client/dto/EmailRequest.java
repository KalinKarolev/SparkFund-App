package app.email.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequest {

    @NotNull
    private String userEmail;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}
