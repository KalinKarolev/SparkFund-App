package app.donation;

import app.donation.model.Donation;
import app.donation.repository.DonationRepository;
import app.donation.service.DonationService;
import app.email.service.EmailService;
import app.spark.model.Spark;
import app.spark.service.SparkService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.TotalDonationsInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DonationServiceUTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private SparkService sparkService;
    @Mock
    private WalletService walletService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DonationService donationService;

    private User user1, user2, user3;
    private Wallet wallet1, wallet2, wallet3, wallet4;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .username("user1")
                .profilePicture("www.pic1.com")
                .build();
        user2 = User.builder()
                .username("user2")
                .profilePicture("www.pic2.com")
                .build();
        user3 = User.builder()
                .username("user3")
                .profilePicture("www.pic3.com")
                .build();
        User user4 = User.builder()
                .username("user4")
                .profilePicture("www.pic4.com")
                .build();

        Donation donation1 = Donation.builder()
                .amount(BigDecimal.valueOf(10))
                .build();
        Donation donation2 = Donation.builder()
                .amount(BigDecimal.valueOf(5))
                .build();
        Donation donation3 = Donation.builder()
                .amount(BigDecimal.valueOf(2))
                .build();
        Donation donation4 = Donation.builder()
                .amount(BigDecimal.valueOf(1.1))
                .build();

        wallet1 = Wallet.builder()
                .owner(user1)
                .donations(List.of(donation1, donation2, donation3, donation4))
                .build();
        wallet2 = Wallet.builder()
                .owner(user2)
                .donations(List.of(donation1, donation2, donation3))
                .build();
        wallet3 = Wallet.builder()
                .owner(user3)
                .donations(List.of(donation1, donation2))
                .build();
        wallet4 = Wallet.builder()
                .owner(user4)
                .donations(List.of(donation1))
                .build();
    }

    @Test
    void givenGetTotalDonationInfo_returnTopThreeDonors() {
        when(walletService.findAllWallets()).thenReturn(List.of(wallet1, wallet2, wallet3, wallet4));
        when(sparkService.findAllSparksWithDonations()).thenReturn(List.of(new Spark(), new Spark(), new Spark(), new Spark()));

        TotalDonationsInfo donationsInfo = donationService.getTotalDonationsInfo();
        assertNotNull(donationsInfo);
        assertEquals(BigDecimal.valueOf(60), donationsInfo.getTotalMoneyRaised());
        assertEquals(4, donationsInfo.getTotalSparksFunded());
        assertEquals(user1.getProfilePicture(), donationsInfo.getFirstDonorImage());
        assertEquals(user1.getUsername(), donationsInfo.getFirstDonorName());
        assertEquals(BigDecimal.valueOf(18.1), donationsInfo.getFirstDonorDonations());
        assertEquals(user2.getProfilePicture(), donationsInfo.getSecondDonorImage());
        assertEquals(user2.getUsername(), donationsInfo.getSecondDonorName());
        assertEquals(BigDecimal.valueOf(17), donationsInfo.getSecondDonorDonations());
        assertEquals(user3.getProfilePicture(), donationsInfo.getThirdDonorImage());
        assertEquals(user3.getUsername(), donationsInfo.getThirdDonorName());
        assertEquals(BigDecimal.valueOf(15), donationsInfo.getThirdDonorDonations());
    }

    @Test
    void givenGetTotalDonationInfo_whenThereIsOnlyOneDonor_returnOneDonors() {
        when(walletService.findAllWallets()).thenReturn(List.of(wallet1));
        when(sparkService.findAllSparksWithDonations()).thenReturn(List.of(new Spark(), new Spark(), new Spark(), new Spark()));

        TotalDonationsInfo donationsInfo = donationService.getTotalDonationsInfo();
        assertNotNull(donationsInfo);
        assertEquals(BigDecimal.valueOf(18), donationsInfo.getTotalMoneyRaised());
        assertEquals(4, donationsInfo.getTotalSparksFunded());
        assertEquals(user1.getProfilePicture(), donationsInfo.getFirstDonorImage());
        assertEquals(user1.getUsername(), donationsInfo.getFirstDonorName());
        assertEquals(BigDecimal.valueOf(18.1), donationsInfo.getFirstDonorDonations());
        assertNull(donationsInfo.getSecondDonorImage());
        assertNull(donationsInfo.getSecondDonorName());
        assertEquals(BigDecimal.ZERO, donationsInfo.getSecondDonorDonations());
        assertNull(donationsInfo.getThirdDonorImage());
        assertNull(donationsInfo.getThirdDonorName());
        assertEquals(BigDecimal.ZERO, donationsInfo.getThirdDonorDonations());
    }

    @Test
    void givenGetTotalDonationInfo_whenThereIsOnlyOneDonor_andSecondUserWithoutDonations_returnTheDonorsAndFilterOutTheOtherUser() {
        wallet2.setDonations(new ArrayList<>());
        when(walletService.findAllWallets()).thenReturn(List.of(wallet1, wallet2));
        when(sparkService.findAllSparksWithDonations()).thenReturn(List.of(new Spark(), new Spark(), new Spark(), new Spark()));

        TotalDonationsInfo donationsInfo = donationService.getTotalDonationsInfo();
        assertNotNull(donationsInfo);
        assertEquals(BigDecimal.valueOf(18), donationsInfo.getTotalMoneyRaised());
        assertEquals(4, donationsInfo.getTotalSparksFunded());
        assertEquals(user1.getProfilePicture(), donationsInfo.getFirstDonorImage());
        assertEquals(user1.getUsername(), donationsInfo.getFirstDonorName());
        assertEquals(BigDecimal.valueOf(18.1), donationsInfo.getFirstDonorDonations());
        assertNull(donationsInfo.getSecondDonorImage());
        assertNull(donationsInfo.getSecondDonorName());
        assertEquals(BigDecimal.ZERO, donationsInfo.getSecondDonorDonations());
        assertNull(donationsInfo.getThirdDonorImage());
        assertNull(donationsInfo.getThirdDonorName());
        assertEquals(BigDecimal.ZERO, donationsInfo.getThirdDonorDonations());
    }
}
