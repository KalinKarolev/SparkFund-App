package app.integration;

import app.donation.model.Donation;
import app.donation.service.DonationService;
import app.spark.model.Spark;
import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.model.UserStatus;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import app.web.dto.DonationRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
public class DonationITest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SparkService sparkService;
    @Autowired
    private SparkRepository sparkRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private DonationService donationService;

    @Test
    void registerDonorAndDonateToSpark_happyPath() {
        //Register the Spark creator
        RegisterRequest sparkCreatorRegisterRequest = RegisterRequest.builder()
                .username("Petar")
                .email("Petar@gmail.com")
                .password("12345")
                .confirmPassword("12345")
                .build();
        User sparkCreator = userService.register(sparkCreatorRegisterRequest);
        // Create a Spark
        Spark spark = Spark.builder()
                .creator(sparkCreator)
                .title("Spark Title Updated")
                .description("Spark Description for integration testing purposes Updated")
                .goalAmount(BigDecimal.TEN.add(BigDecimal.TEN))
                .currentAmount(BigDecimal.ONE)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.newPic1.com")
                .secondPictureUrl("www.newPic2.com")
                .thirdPictureUrl("www.newPic3.com")
                .createdOn(LocalDateTime.now())
                .build();
        sparkRepository.save(spark);

        // Register user who will donate to the Spark
        RegisterRequest donorRegisterRequest = RegisterRequest.builder()
                .username("Kalin")
                .email("kalin@gmail.com")
                .password("12345")
                .confirmPassword("12345")
                .build();
        User donor = userService.register(donorRegisterRequest);

        // Add funds to the Donor wallet
        donor = userService.getUserById(donor.getId());
        assertNotNull(donor.getWallet());
        Wallet donorWallet = donor.getWallet();
        walletService.addFunds(donorWallet, BigDecimal.valueOf(100), UserStatus.ACTIVE);
        assertEquals(0, donorWallet.getAmount().compareTo(BigDecimal.valueOf(100)));

        // Create the donation
        DonationRequest donationRequest = DonationRequest.builder()
                .amount(BigDecimal.valueOf(50))
                .message("Donation message")
                .build();
        donationService.addDonationToSpark(donor, spark, donationRequest);

        spark = sparkService.getSparkById(spark.getId());
        assertEquals(1, spark.getDonations().size());
        Donation donation = spark.getDonations().get(0);
        assertEquals(0, BigDecimal.valueOf(51).compareTo(spark.getCurrentAmount()));
        assertEquals(0, BigDecimal.valueOf(50).compareTo(donation.getAmount()));
        assertEquals(donationRequest.getMessage(), donation.getMessage());
        assertEquals(spark, donation.getSpark());
        assertNotNull(donation.getCreatedOn());
        assertEquals(0, BigDecimal.valueOf(50).compareTo(donor.getWallet().getAmount()));
    }
}
