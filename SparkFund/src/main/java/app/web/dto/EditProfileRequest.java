package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileRequest {

    @NotBlank(message = "Username should not be empty")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 symbols")
    private String username;

    @Size(max = 20, message = "First name can`t be more than 20 symbols")
    private String firstName;

    @Size(max = 20, message = "Last name can`t be more than 20 symbols")
    private String lastName;

    @NotBlank(message = "Email should not be empty")
    @Email(message = "Requires correct email format")
    private String email;

    @URL
    private String profilePicture;

}
