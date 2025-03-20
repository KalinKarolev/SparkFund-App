package app.web.mapper;

import app.spark.model.Spark;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.web.dto.EditProfileRequest;
import app.web.dto.ManageSparkRequest;
import app.web.dto.UserSignalRequest;
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

    public static UserSignalRequest mapUserSignalToUserSignalRequest(UserSignal userSignal) {
        return UserSignalRequest.builder()
                .id(userSignal.getId())
                .adminResponse(userSignal.getAdminResponse())
                .creatorUsername(userSignal.getCreator().getUsername())
                .title(userSignal.getTitle())
                .message(userSignal.getMessage())
                .status(userSignal.getUserSignalStatus())
                .createdOn(userSignal.getCreatedOn())
                .updatedOn(userSignal.getUpdatedOn())
                .build();
    }
}
