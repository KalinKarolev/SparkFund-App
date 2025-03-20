package app.web.dto;

import app.usersignal.model.UserSignalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignalRequest {

    private UUID id;

    private String creatorUsername;

    @NotBlank (message = "Your signal should have title")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 20 symbols")
    private String title;

    @NotBlank(message = "Message should not be empty")
    @Size(min = 5, max = 1000, message = "Message must be between 5 and 1000 symbols")
    private String message;

    private UserSignalStatus status;

    private String adminResponse;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
