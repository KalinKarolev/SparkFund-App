package app.donation.service;

import app.donation.model.Donation;
import app.donation.repository.DonationRepository;
import app.email.service.EmailService;
import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.model.UserStatus;
import app.util.CommonUtils;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.DonationRequest;
import app.web.dto.TotalDonationsInfo;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final SparkService sparkService;
    private final WalletService walletService;
    private final EmailService emailService;

    public DonationService(DonationRepository _donationRepository, SparkService _sparkService, WalletService walletService, EmailService _emailService) {
        donationRepository = _donationRepository;
        sparkService = _sparkService;
        this.walletService = walletService;
        emailService = _emailService;
    }

    @Transactional
    public void addDonationToSpark(User user, Spark spark, DonationRequest donationRequest) {
        // Validate that the donation can be done
        validateDonation(user, spark);

        walletService.reduceAmount(user, donationRequest.getAmount());
        Donation donation = Donation.builder()
                .amount(donationRequest.getAmount())
                .message(donationRequest.getMessage())
                .wallet(user.getWallet())
                .spark(spark)
                .createdOn(LocalDateTime.now())
                .build();
        sparkService.increaseCurrentAmount(spark, donationRequest.getAmount());
        donationRepository.save(donation);
    }

    /**
     * Computes and returns donation-related statistics, including total donations, total funded sparks,
     * and the top 3 donors based on donation amounts.
     *
     * <p>The method performs the following operations:</p>
     * <ul>
     *     <li>Retrieves all wallets and calculates the total donations per wallet owner.</li>
     *     <li>Determines the total sum of all donations.</li>
     *     <li>Filters and ranks wallets with at least 1 in total donations, selecting the top 3.</li>
     *     <li>Retrieves the number of sparks that have received donations.</li>
     *     <li>Extracts details (profile picture, username, and total donation amount) for the top 3 donors.</li>
     * </ul>
     */
    public TotalDonationsInfo getTotalDonationsInfo() {
        List<Wallet> wallets = walletService.findAllWallets();

        Map<String, BigDecimal> walletDonationsMap = new HashMap<>();
        BigDecimal totalDonations = BigDecimal.ZERO;

        // Calculate total donation per wallet owner and overall total
        for (Wallet wallet : wallets) {
            String ownerUsername = wallet.getOwner().getUsername();
            BigDecimal walletTotal = getSumOfWalletDonations(wallet);

            walletDonationsMap.merge(ownerUsername, walletTotal, BigDecimal::add);
            totalDonations = totalDonations.add(walletTotal);
        }

        // Get the top 3 wallets with the highest donation sum and filter out wallets without donations
        List<Wallet> topWallets = wallets.stream()
                .filter(wallet ->
                        walletDonationsMap.getOrDefault(wallet.getOwner().getUsername(), BigDecimal.ZERO)
                        .compareTo(BigDecimal.ONE) >= 0)
                .sorted(Comparator.comparing(wallet ->
                        walletDonationsMap.getOrDefault(wallet.getOwner().getUsername(), BigDecimal.ZERO), Comparator.reverseOrder()))
                .limit(3)
                .toList();

        int totalSparksFunded = sparkService.findAllSparksWithDonations().size();

        String firstDonorImage = !topWallets.isEmpty() ? topWallets.get(0).getOwner().getProfilePicture() : null;
        String firstDonorName = !topWallets.isEmpty() ? topWallets.get(0).getOwner().getUsername() : null;
        BigDecimal firstDonorDonations = !topWallets.isEmpty() ? getSumOfWalletDonations(topWallets.get(0)) : BigDecimal.ZERO;

        String secondDonorImage = topWallets.size() > 1 ? topWallets.get(1).getOwner().getProfilePicture() : null;
        String secondDonorName = topWallets.size() > 1 ? topWallets.get(1).getOwner().getUsername() : null;
        BigDecimal secondDonorDonations = topWallets.size() > 1 ? getSumOfWalletDonations(topWallets.get(1)) : BigDecimal.ZERO;

        String thirdDonorImage = topWallets.size() > 2 ? topWallets.get(2).getOwner().getProfilePicture() : null;
        String thirdDonorName = topWallets.size() > 2 ? topWallets.get(2).getOwner().getUsername() : null;
        BigDecimal thirdDonorDonations = topWallets.size() > 2 ? getSumOfWalletDonations(topWallets.get(2)) : BigDecimal.ZERO;


        return TotalDonationsInfo.builder()
                .totalMoneyRaised(totalDonations.setScale(0, RoundingMode.HALF_UP))
                .totalSparksFunded(totalSparksFunded)
                .firstDonorImage(firstDonorImage)
                .firstDonorName(firstDonorName)
                .firstDonorDonations(firstDonorDonations)
                .secondDonorImage(secondDonorImage)
                .secondDonorName(secondDonorName)
                .secondDonorDonations(secondDonorDonations)
                .thirdDonorImage(thirdDonorImage)
                .thirdDonorName(thirdDonorName)
                .thirdDonorDonations(thirdDonorDonations)
                .build();
    }

    private static BigDecimal getSumOfWalletDonations(Wallet wallet) {
        return wallet.getDonations().stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateDonation(User user, Spark spark) {
        if(user.getUserStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("Action denied: Inactive user cannot make donation.");
        } else if (spark.getStatus() != SparkStatus.ACTIVE) {
            throw new AccessDeniedException("Action denied: You cannot donate to Spark that is not active.");
        }
    }

    public void sendEmailForDonation(Spark spark, String donorName, String message, BigDecimal amount) {
        String emailSubject = "Your Spark received donation";
        String emailBody = String.format("Your Spark [%s] received donation for %s euro from donor with username: %s", spark.getTitle(), amount.toString(), donorName);
        if (CommonUtils.isNotEmpty(message)) {
            emailBody += String.format("%n%n They sent you the following message: %s", message);
        }
        emailService.sendEmail(spark.getCreator().getEmail(), emailSubject, emailBody);
    }
}
