package app.spark.service;

import app.donation.model.Donation;
import app.email.service.EmailService;
import app.exceptions.ResourceNotFoundException;
import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.user.model.User;
import app.user.model.UserStatus;
import app.util.CommonUtils;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.ManageSparkRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SparkService {

    private final SparkRepository sparkRepository;
    private final WalletService walletService;
    private final EmailService emailService;

    public SparkService(SparkRepository _sparkRepository, WalletService _walletService, EmailService _emailService) {
        sparkRepository = _sparkRepository;
        walletService = _walletService;
        emailService = _emailService;
    }

    @Transactional
    public Spark createSpark(ManageSparkRequest manageSparkRequest, User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("Action denied: Inactive users cannot create new Sparks.");
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
            throw new AccessDeniedException("Action denied: Inactive users cannot update Sparks.");
        }
        if (spark.getStatus() != SparkStatus.ACTIVE) {
            throw new AccessDeniedException("Action denied: Only active Sparks can be updated.");
        }
        spark.setTitle(manageSparkRequest.getTitle());
        spark.setDescription(manageSparkRequest.getDescription());
        spark.setGoalAmount(manageSparkRequest.getGoalAmount());
        spark.setCategory(manageSparkRequest.getCategory());
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
            return getAllActiveSparks();
        }
        List<Spark> filteredSparksByStatus = new ArrayList<>(sparkRepository.findAllByOrderByCreatedOnDesc().stream()
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
    public void filterSparksByOwner(User user, List<Spark> filteredSparks, String ownership) {
        if ("MY_SPARKS".equals(ownership)) {
            filteredSparks.removeIf(spark -> !spark.getCreator().equals(user));
        } else if ("SPARKS_I_DONATE_TO".equals(ownership)) {
            filteredSparks.removeIf(spark ->
                    spark.getDonations().stream()
                            .noneMatch(donation -> donation.getWallet() == null
                                    || donation.getWallet().equals(user.getWallet()))
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
                .orElseThrow(() -> new ResourceNotFoundException("No spark found with ID: " + id));
    }

    /**
     * Cancels the specified Spark by returning all donations to the donors' wallets,
     * nullifying the current amount of the Spark, and setting its status to CANCELLED.
     *
     * @param spark The Spark to be cancelled.
     */
    public void cancelSparkAndReturnDonations(Spark spark) {
        if (!CommonUtils.isZeroAmount(spark.getCurrentAmount()) && !spark.getDonations().isEmpty()) {
            List<Donation> donations = spark.getDonations();
            for (Donation donation : donations) {
                Wallet donatorWallet = donation.getWallet();
                walletService.addFundsWithoutUserValidation(donatorWallet, donation.getAmount());
                sendEmailForSparkCancellation(donatorWallet.getOwner().getEmail(), spark.getTitle(), donation.getAmount());
            }
            spark.setCurrentAmount(BigDecimal.ZERO);
        }
        spark.setStatus(SparkStatus.CANCELLED);
        spark.setUpdatedOn(LocalDateTime.now());
        sparkRepository.save(spark);
    }

    public List<Spark> findSparksForCompletion() {
        return sparkRepository.findAllWhereStatusActiveAndCurrentAmountIsGreaterThanOrEqualToGoalAmount();
    }

    public void completeSpark(Spark spark) {
        spark.setStatus(SparkStatus.COMPLETED);
        spark.setUpdatedOn(LocalDateTime.now());
        sparkRepository.save(spark);
        sendEmailForSparkCompletion(spark);
    }

    private void sendEmailForSparkCompletion(Spark spark) {
        String emailSubject = "Your Spark is completed";
        String emailBody = String.format("Your Spark [%s] is completed after the goal amount of %s was raised!", spark.getTitle(), spark.getGoalAmount().toString());
        emailService.sendEmail(spark.getCreator().getEmail(), emailSubject, emailBody);
    }

    private void sendEmailForSparkCancellation(String donatorEmail, String sparkTitle, BigDecimal donationAmount) {
        String emailSubject = "Refund from SparkFund";
        String emailBody = String.format("Spark [%s] was cancelled and your donation for %s euro is refunded", sparkTitle, donationAmount.toString());
        emailService.sendEmail(donatorEmail, emailSubject, emailBody);
    }

    public List<Spark> findAllSparksWithDonations() {
        return sparkRepository.findAllByCurrentAmountGreaterThan(BigDecimal.ZERO);
    }

    /**
     * Retrieves all Sparks in status ACTIVE ordered by date of creation in descending order.
     */
    public List<Spark> getAllActiveSparks() {
        return sparkRepository.findAllByStatusOrderByCreatedOnDesc(SparkStatus.ACTIVE);
    }
}
