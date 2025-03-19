package app.donation.service;

import app.donation.model.Donation;
import app.donation.repository.DonationRepository;
import app.exceptions.DomainException;
import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.model.UserStatus;
import app.wallet.service.WalletService;
import app.web.dto.DonationRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final SparkService sparkService;
    private final WalletService walletService;

    public DonationService(DonationRepository _donationRepository, SparkService _sparkService, WalletService walletService) {
        donationRepository = _donationRepository;
        sparkService = _sparkService;
        this.walletService = walletService;
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

    private void validateDonation(User user, Spark spark) {
        if(user.getUserStatus() != UserStatus.ACTIVE) {
            throw new DomainException("Action denied: Inactive user cannot make donation.");
        } else if (spark.getStatus() != SparkStatus.ACTIVE) {
            throw new DomainException("Action denied: You cannot donate to Spark that is not active.");
        }
    }


}
