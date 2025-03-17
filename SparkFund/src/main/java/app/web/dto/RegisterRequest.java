package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank (message = "Username should not be empty")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 symbols")
    private String username;

    @NotBlank (message = "Email should not be empty")
    @Email(message = "Requires correct email format")
    private String email;

    @NotBlank (message = "Password should not be empty")
    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 symbols")
    private String password;

    @NotBlank (message = "Password should not be empty")
    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 symbols")
    private String confirmPassword;

}
