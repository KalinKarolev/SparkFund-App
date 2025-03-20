package app.web.mapper;

import app.spark.model.Spark;
import app.user.model.User;
import app.web.dto.EditProfileRequest;
import app.web.dto.ManageSparkRequest;
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

    public static ManageSparkRequest mapSparkToManageSparkRequest(Spark spark) {
        return ManageSparkRequest.builder()
                .id(spark.getId())
                .title(spark.getTitle())
                .description(spark.getDescription())
                .goalAmount(spark.getGoalAmount())
                .category(spark.getCategory())
                .status(spark.getStatus())
                .firstPictureUrl(spark.getFirstPictureUrl())
                .secondPictureUrl(spark.getSecondPictureUrl())
                .thirdPictureUrl(spark.getThirdPictureUrl())
                .build();
    }
}
