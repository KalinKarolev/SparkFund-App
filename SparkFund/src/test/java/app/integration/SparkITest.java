package app.integration;

import app.spark.model.Spark;
import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ManageSparkRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class SparkITest {

    @Autowired
    private SparkService sparkService;
    @Autowired
    private UserService userService;
    @Autowired
    private SparkRepository sparkRepository;

    @Test
    void createAndUpdateSpark_happyPath() {
        // Register user who will create a Spark
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Admin")
                .email("admin@gmail.com")
                .password("12345")
                .confirmPassword("12345")
                .build();
        User registeredUser = userService.register(registerRequest);

        //Create new Spark
        ManageSparkRequest createSparkRequest = ManageSparkRequest.builder()
                .title("Spark Title")
                .description("Spark Description for integration testing purposes")
                .goalAmount(BigDecimal.TEN)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.pic1.com")
                .secondPictureUrl("www.pic2.com")
                .thirdPictureUrl("www.pic3.com")
                .build();
        Spark spark = sparkService.createSpark(createSparkRequest, registeredUser);
        assertNotNull(spark);
        assertNotNull(spark.getId());
        assertNotNull(spark.getCreatedOn());
        assertNull(spark.getUpdatedOn());
        assertEquals(registeredUser, spark.getCreator());
        assertEquals(createSparkRequest.getTitle(), spark.getTitle());
        assertEquals(createSparkRequest.getDescription(), spark.getDescription());
        assertEquals(BigDecimal.ZERO, spark.getCurrentAmount());
        assertEquals(createSparkRequest.getGoalAmount(), spark.getGoalAmount());
        assertEquals(createSparkRequest.getCategory(), spark.getCategory());
        assertEquals(SparkStatus.ACTIVE, spark.getStatus());
        assertEquals(createSparkRequest.getFirstPictureUrl(), spark.getFirstPictureUrl());
        assertEquals(createSparkRequest.getSecondPictureUrl(), spark.getSecondPictureUrl());
        assertEquals(createSparkRequest.getThirdPictureUrl(), spark.getThirdPictureUrl());

        // Update Spark current amount (donation simulation)
        sparkService.increaseCurrentAmount(spark, BigDecimal.ONE);

        // Update the Spark from the current User
        ManageSparkRequest updateSparkRequest = ManageSparkRequest.builder()
                .title("Spark Title Updated")
                .description("Spark Description for integration testing purposes Updated")
                .goalAmount(BigDecimal.TEN.add(BigDecimal.TEN))
                .currentAmount(BigDecimal.ONE)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.newPic1.com")
                .secondPictureUrl("www.newPic2.com")
                .thirdPictureUrl("www.newPic3.com")
                .build();
        sparkService.updateSpark(updateSparkRequest, spark, registeredUser);

        Optional<Spark> updatedSparkOptional = sparkRepository.findById(spark.getId());
        assertTrue(updatedSparkOptional.isPresent());
        Spark updatedSpark = updatedSparkOptional.get();
        assertNotNull(spark.getCreatedOn());
        assertNotNull(spark.getUpdatedOn());
        assertEquals(updateSparkRequest.getTitle(), updatedSpark.getTitle());
        assertEquals(updateSparkRequest.getDescription(), updatedSpark.getDescription());
        assertEquals(0, updatedSpark.getCurrentAmount().compareTo(BigDecimal.ONE));
        assertEquals(0, updateSparkRequest.getGoalAmount().compareTo(updatedSpark.getGoalAmount()));
        assertEquals(updateSparkRequest.getCategory(), updatedSpark.getCategory());
        assertEquals(SparkStatus.ACTIVE, updatedSpark.getStatus());
        assertEquals(updateSparkRequest.getFirstPictureUrl(), updatedSpark.getFirstPictureUrl());
        assertEquals(updateSparkRequest.getSecondPictureUrl(), updatedSpark.getSecondPictureUrl());
        assertEquals(updateSparkRequest.getThirdPictureUrl(), updatedSpark.getThirdPictureUrl());
    }
}
