package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 symbols")
    private String username;

    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 symbols")
    private String password;
}
