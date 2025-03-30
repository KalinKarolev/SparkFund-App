package app.web.mapper;

import app.spark.model.Spark;
import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.web.dto.EditProfileRequest;
import app.web.dto.ManageSparkRequest;
import app.web.dto.UserSignalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void testMapUserToEditProfileRequest(){
        User user = User.builder()
                .username("Kalin123")
                .firstName("Kalin")
                .lastName("Karolev")
                .email("kalin@gmail.com")
                .profilePicture("www.pic.com")
                .build();
        EditProfileRequest result = DtoMapper.mapUserToEditProfileRequest(user);

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getProfilePicture(), result.getProfilePicture());
    }

    @Test
    void testMapSparkToManageSparkRequest() {
        Spark spark = Spark.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .description("Description")
                .goalAmount(BigDecimal.TEN)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.pic1.com")
                .secondPictureUrl("www.pic2.com")
                .thirdPictureUrl("www.pic3.com")
                .build();
        ManageSparkRequest result = DtoMapper.mapSparkToManageSparkRequest(spark);

        assertEquals(spark.getId(), result.getId());
        assertEquals(spark.getTitle(), result.getTitle());
        assertEquals(spark.getDescription(), result.getDescription());
        assertEquals(spark.getGoalAmount(), result.getGoalAmount());
        assertEquals(spark.getCategory(), result.getCategory());
        assertEquals(spark.getStatus(), result.getStatus());
        assertEquals(spark.getFirstPictureUrl(), result.getFirstPictureUrl());
        assertEquals(spark.getSecondPictureUrl(), result.getSecondPictureUrl());
        assertEquals(spark.getThirdPictureUrl(), result.getThirdPictureUrl());
    }

    @Test
    void testMapUserSignalToUserSignalRequest() {
        LocalDateTime cratedDate = LocalDateTime.of(2010, 3, 29, 12, 0, 0);
        LocalDateTime updatedDate = LocalDateTime.of(2010, 3, 30, 12, 0, 0);

        User user = User.builder()
                .username("Kalin123")
                .build();
        UserSignal userSignal = UserSignal.builder()
                .id(UUID.randomUUID())
                .adminResponse("Response")
                .creator(user)
                .title("Title")
                .message("Message")
                .userSignalStatus(UserSignalStatus.PENDING)
                .createdOn(cratedDate)
                .updatedOn(updatedDate)
                .build();
        UserSignalRequest result = DtoMapper.mapUserSignalToUserSignalRequest(userSignal);

        assertEquals(userSignal.getId(), result.getId());
        assertEquals(userSignal.getAdminResponse(), result.getAdminResponse());
        assertEquals(user.getUsername(), result.getCreatorUsername());
        assertEquals(userSignal.getTitle(), result.getTitle());
        assertEquals(userSignal.getMessage(), result.getMessage());
        assertEquals(userSignal.getUserSignalStatus(), result.getStatus());
        assertEquals(userSignal.getCreatedOn(), result.getCreatedOn());
        assertEquals(userSignal.getUpdatedOn(), result.getUpdatedOn());
    }
}
