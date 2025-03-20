package app.spark.service;

import app.exceptions.DomainException;
import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.user.model.User;
import app.user.model.UserStatus;
import app.util.CommonUtils;
import app.web.dto.ManageSparkRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SparkService {

    private final SparkRepository sparkRepository;

    public SparkService(SparkRepository _sparkRepository) {
        sparkRepository = _sparkRepository;
    }

    @Transactional
    public Spark createSpark(ManageSparkRequest manageSparkRequest, User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new DomainException("Action denied: Inactive users cannot create new Sparks.");
        }
        Spark spark = Spark.builder()
                .creator(user)
                .title(manageSparkRequest.getTitle())
                .description(manageSparkRequest.getDescription())
                .currentAmount(manageSparkRequest.getCurrentAmount() != null ? manageSparkRequest.getCurrentAmount() : BigDecimal.ZERO)
                .goalAmount(manageSparkRequest.getGoalAmount())
                .category(manageSparkRequest.getCategory())
                .status(manageSparkRequest.getStatus())
                .firstPictureUrl(manageSparkRequest.getFirstPictureUrl())
                .secondPictureUrl(manageSparkRequest.getSecondPictureUrl())
                .thirdPictureUrl(manageSparkRequest.getThirdPictureUrl())
                .createdOn(LocalDateTime.now())
                .build();
        return sparkRepository.save(spark);
    }

    @Transactional
    public void updateSpark(ManageSparkRequest manageSparkRequest, Spark spark, User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new DomainException("Action denied: Inactive users cannot update Sparks.");
        }
        if (spark.getStatus() != SparkStatus.ACTIVE) {
            throw new DomainException("Action denied: Only active Sparks can be updated.");
        }
        spark.setTitle(manageSparkRequest.getTitle());
        spark.setDescription(manageSparkRequest.getDescription());
        spark.setGoalAmount(manageSparkRequest.getGoalAmount());
        spark.setCategory(manageSparkRequest.getCategory());
        spark.setStatus(manageSparkRequest.getStatus());
        spark.setFirstPictureUrl(manageSparkRequest.getFirstPictureUrl());
        spark.setSecondPictureUrl(manageSparkRequest.getSecondPictureUrl());
        spark.setThirdPictureUrl(manageSparkRequest.getThirdPictureUrl());
        spark.setUpdatedOn(LocalDateTime.now());
        sparkRepository.save(spark);
    }

    /**
     * Retrieves a list of Sparks that match the provided filtering criteria (status, category, and ownership).
     * If the screen for All Sparks is loaded for the first time, all active Sparks will be returned.
     *
     * @param user The User who is viewing the All Sparks screen.
     * @param status The status of the Sparks to filter by.
     * @param category The category of the Sparks to filter by.
     * @param ownership The ownership filter (e.g., "ALL_SPARKS", "MY_SPARKS", "SPARKS_I_DONATE_TO").
     * @return A list of filtered Sparks based on the provided criteria.
     */
    public List<Spark> getAllSparks(User user, String status, String category, String ownership) {
        if (CommonUtils.areAllNull(status, category, ownership)) {
            return sparkRepository.findAllByStatusOrderByCreatedOnDesc(SparkStatus.ACTIVE);
        }
        List<Spark> filteredSparksByStatus = new java.util.ArrayList<>(sparkRepository.findAllByOrderByCreatedOnDesc().stream()
                .filter(spark -> spark.getStatus().name().equals(status))
                .toList());
        if (!"ALL".equals(category)) {
            filteredSparksByStatus.removeIf(spark -> !spark.getCategory().name().equals(category));
        }
        if (!"ALL_SPARKS".equals(ownership)) {
            filterSparksByOwner(user, filteredSparksByStatus, ownership);
        }
        return filteredSparksByStatus;
    }

    /**
     * Filters the provided list of Sparks based on the ownership criteria.
     * The Sparks are filtered depending on whether the user is the creator of the Spark
     * or if the user has made donations to the Spark's associated wallet.
     *
     * @param user The User whose ownership or donations will be used for filtering.
     * @param filteredSparks The list of Sparks to filter based on ownership.
     * @param ownership The ownership filter criteria ("MY_SPARKS" or "SPARKS_I_DONATE_TO").
     */
    private void filterSparksByOwner(User user, List<Spark> filteredSparks, String ownership) {
        if ("MY_SPARKS".equals(ownership)) {
            filteredSparks.removeIf(spark -> !spark.getCreator().equals(user));
        } else if ("SPARKS_I_DONATE_TO".equals(ownership)) {
            filteredSparks.removeIf(spark ->
                    spark.getDonations().stream()
                            .noneMatch(donation -> donation.getWallet().equals(user.getWallet()))
            );
        }

    }

    public void increaseCurrentAmount(Spark spark, BigDecimal amount) {
        BigDecimal currentAmount = spark.getCurrentAmount() != null ? spark.getCurrentAmount() : BigDecimal.ZERO;
        spark.setCurrentAmount(currentAmount.add(amount));
        spark.setUpdatedOn(LocalDateTime.now());
        sparkRepository.save(spark);
    }

    public Spark getSparkById(UUID id) {
        return sparkRepository.findById(id)
                .orElseThrow(() -> new DomainException("No spark found with ID: " + id));
    }
}
