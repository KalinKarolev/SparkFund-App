package app.web.mapper;

import app.user.model.User;
import app.web.dto.EditProfileRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static EditProfileRequest mapUserToEditProfileRequest(User user) {
        return EditProfileRequest.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .isAnonymousDonator(user.getIsAnonymousDonator())
                .build();
    }
}
