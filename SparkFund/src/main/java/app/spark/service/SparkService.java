package app.spark.service;

import app.exceptions.DomainException;
import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.user.model.User;
import app.user.model.UserStatus;
import app.web.dto.ManageSparkRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SparkService {

    private final SparkRepository sparkRepository;

    public SparkService(SparkRepository _sparkRepository) {
        sparkRepository = _sparkRepository;
    }

    @Transactional
    public void manageSpark(ManageSparkRequest manageSparkRequest, User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new DomainException("Action denied: Inactive users cannot create new Sparks.");
        }
        Spark spark = Spark.builder()
                .creator(user)
                .title(manageSparkRequest.getTitle())
                .description(manageSparkRequest.getDescription())
                .goalAmount(manageSparkRequest.getGoalAmount())
                .category(manageSparkRequest.getCategory())
                .status(manageSparkRequest.getStatus())
                .firstPictureUrl(manageSparkRequest.getFirstPictureUrl())
                .secondPictureUrl(manageSparkRequest.getSecondPictureUrl())
                .thirdPictureUrl(manageSparkRequest.getThirdPictureUrl())
                .build();
        if (spark.getCreatedOn() == null) {
            spark.setCreatedOn(LocalDateTime.now());
        } else {
            spark.setUpdatedOn(LocalDateTime.now());
        }
        sparkRepository.save(spark);
    }

    public List<Spark> getAllActiveSparks() {
        return sparkRepository.findAllByStatus(SparkStatus.ACTIVE);
    }
}
