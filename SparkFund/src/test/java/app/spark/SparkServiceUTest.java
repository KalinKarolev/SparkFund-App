package app.spark;

import app.donation.model.Donation;
import app.email.service.EmailService;
import app.spark.model.Spark;
import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import app.spark.repostiroty.SparkRepository;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.model.UserStatus;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.ManageSparkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SparkServiceUTest {

    @Mock
    private SparkRepository sparkRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private SparkService sparkService;

    private SparkService spySparkService;
    private User user;
    private Spark spark1, spark2, spark3;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userStatus(UserStatus.ACTIVE)
                .id(UUID.randomUUID())
                .username("Kalin")
                .build();

        spark1 = Spark.builder()
                .id(UUID.randomUUID())
                .status(SparkStatus.ACTIVE)
                .category(SparkCategory.EDUCATION)
                .creator(user)
                .donations(new ArrayList<>())
                .build();

        spark2 = Spark.builder()
                .id(UUID.randomUUID())
                .status(SparkStatus.CANCELLED)
                .category(SparkCategory.HEALTH)
                .creator(user)
                .donations(new ArrayList<>())
                .build();

        spark3 = Spark.builder()
                .id(UUID.randomUUID())
                .status(SparkStatus.ACTIVE)
                .category(SparkCategory.HEALTH)
                .creator(new User())
                .donations(new ArrayList<>())
                .build();
        spySparkService = spy(sparkService);
    }

    @Test
    void givenUserIsNotActive_whenCreateSpark_thenThrowsException() {
        User user = User.builder()
                .userStatus(UserStatus.INACTIVE)
                .build();
        assertThrows(AccessDeniedException.class, () -> sparkService.createSpark(new ManageSparkRequest(), user));
    }

    @Test
    void givenUserIsActive_whenCreateSpark_thenCreateNewSpark() {
        ManageSparkRequest sparkRequest = ManageSparkRequest.builder()
                .title("Title")
                .description("Description")
                .goalAmount(BigDecimal.TEN)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.pic1.com")
                .secondPictureUrl("www.pic2.com")
                .thirdPictureUrl("www.pic3.com")
                .build();

        sparkService.createSpark(sparkRequest, user);

        ArgumentCaptor<Spark> sparkCaptor = ArgumentCaptor.forClass(Spark.class);
        verify(sparkRepository).save(sparkCaptor.capture());
        Spark savedSpark = sparkCaptor.getValue();

        assertEquals(user, savedSpark.getCreator());
        assertEquals(sparkRequest.getTitle(), savedSpark.getTitle());
        assertEquals(sparkRequest.getDescription(), savedSpark.getDescription());
        assertEquals(BigDecimal.ZERO, savedSpark.getCurrentAmount());
        assertEquals(sparkRequest.getGoalAmount(), savedSpark.getGoalAmount());
        assertEquals(sparkRequest.getCategory(), savedSpark.getCategory());
        assertEquals(sparkRequest.getStatus(), savedSpark.getStatus());
        assertEquals(sparkRequest.getFirstPictureUrl(), savedSpark.getFirstPictureUrl());
        assertEquals(sparkRequest.getSecondPictureUrl(), savedSpark.getSecondPictureUrl());
        assertEquals(sparkRequest.getThirdPictureUrl(), savedSpark.getThirdPictureUrl());
        verify(sparkRepository).save(savedSpark);
    }

    @Test
    void givenUserIsNotActive_whenUpdateSpark_thenThrowsException() {
        User user = User.builder()
                .userStatus(UserStatus.INACTIVE)
                .build();
        assertThrows(AccessDeniedException.class, () -> sparkService.updateSpark(new ManageSparkRequest(), new Spark(), user));
    }

    @Test
    void givenSparkIsNotActive_whenUpdateSpark_thenThrowsException() {
        assertThrows(AccessDeniedException.class, () -> sparkService.updateSpark(new ManageSparkRequest(), spark2, user));
    }

    @Test
    void givenUserAndSparkAreActive_whenUpdateSpark_thenUpdateSpark() {
        ManageSparkRequest sparkRequest = ManageSparkRequest.builder()
                .title("Title")
                .description("Description")
                .currentAmount(BigDecimal.ONE)
                .goalAmount(BigDecimal.TEN)
                .category(SparkCategory.EDUCATION)
                .status(SparkStatus.ACTIVE)
                .firstPictureUrl("www.pic1.com")
                .secondPictureUrl("www.pic2.com")
                .thirdPictureUrl("www.pic3.com")
                .build();

        sparkService.updateSpark(sparkRequest, spark1, user);

        ArgumentCaptor<Spark> sparkCaptor = ArgumentCaptor.forClass(Spark.class);
        verify(sparkRepository).save(sparkCaptor.capture());
        Spark savedSpark = sparkCaptor.getValue();

        assertEquals(sparkRequest.getTitle(), savedSpark.getTitle());
        assertEquals(sparkRequest.getDescription(), savedSpark.getDescription());
        assertEquals(sparkRequest.getGoalAmount(), savedSpark.getGoalAmount());
        assertEquals(sparkRequest.getCategory(), savedSpark.getCategory());
        assertEquals(sparkRequest.getStatus(), savedSpark.getStatus());
        assertEquals(sparkRequest.getFirstPictureUrl(), savedSpark.getFirstPictureUrl());
        assertEquals(sparkRequest.getSecondPictureUrl(), savedSpark.getSecondPictureUrl());
        assertEquals(sparkRequest.getThirdPictureUrl(), savedSpark.getThirdPictureUrl());
        verify(sparkRepository).save(savedSpark);
    }

    @Test
    void givenAllSparksRequired_whenGetAllSparks_thenGetAllActiveSparks() {
        List<Spark> allSparks = List.of(spark1, spark2);

        when(sparkService.getAllActiveSparks()).thenReturn(allSparks);

        List<Spark> result = sparkService.getAllSparks(new User(), null, null, null);
        assertEquals(allSparks, result);
    }

    @Test
    void givenStatusFilter_whenGetAllSparks_thenFilterSparksByStatus() {
        when(sparkRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(spark1, spark2, spark3));

        List<Spark> result = sparkService.getAllSparks(user, "ACTIVE", "ALL", null);
        assertEquals(List.of(spark1, spark3), result);
    }

    @Test
    void givenCategoryFilter_whenGetAllSparks_thenFilterSparksByCategory() {
        when(sparkRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(spark1, spark2, spark3));

        List<Spark> result = sparkService.getAllSparks(user, "ACTIVE", "HEALTH", null);
        assertEquals(List.of(spark3), result);
    }

    @Test
    void givenOwnershipFilter_whenGetAllSparks_thenFilterSparksByOwner() {
        when(sparkRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(spark1, spark2, spark3));

        doNothing().when(spySparkService).filterSparksByOwner(eq(user), anyList(), eq("MY_SPARKS"));

        List<Spark> result = spySparkService.getAllSparks(user, "ACTIVE", "EDUCATION", "MY_SPARKS");
        assertEquals(List.of(spark1), result);
    }

    @Test
    void givenSparksFilteredByOwner_whenFilterSparksByOwner_thenReturnUserSparks() {
        List<Spark> allSparks = new ArrayList<>(Arrays.asList(spark1, spark2, spark3));

        sparkService.filterSparksByOwner(user, allSparks, "MY_SPARKS");
        assertEquals(List.of(spark1, spark2), allSparks);
    }

    @Test
    void givenSparksFilteredByOwner_whenFilterSparksByOwner_thenReturnSparksUserDonateTo() {
        Wallet wallet = new Wallet();
        Donation donation = new Donation();
        wallet.setDonations(List.of(donation));
        user.setWallet(wallet);

        Spark spark4 = Spark.builder()
                .creator(user)
                .donations(List.of(donation))
                .build();

        List<Spark> allSparks = new ArrayList<>(Arrays.asList(spark1, spark2, spark3, spark4));

        sparkService.filterSparksByOwner(user, allSparks, "SPARKS_I_DONATE_TO");
        assertEquals(List.of(spark4), allSparks);
    }

    @Test
    void givenMoneyDonatedToSparkForTheFirstTime_whenIncreaseCurrentAmount_thenAddToSparkCurrentAmount() {
        sparkService.increaseCurrentAmount(spark1, BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, spark1.getCurrentAmount());
        assertNotNull(spark1.getUpdatedOn());
        verify(sparkRepository).save(spark1);
    }

    @Test
    void givenMoneyDonatedToSparkNotForTheFirstTime_whenIncreaseCurrentAmount_thenAddToSparkCurrentAmount() {
        spark1.setCurrentAmount(BigDecimal.TEN);
        sparkService.increaseCurrentAmount(spark1, BigDecimal.TEN);
        assertEquals(new BigDecimal("20"), spark1.getCurrentAmount());
        assertNotNull(spark1.getUpdatedOn());
        verify(sparkRepository).save(spark1);
    }

    @Test
    void givenSparkIsCancelledBeforeDonations_whenCancelSparkAndReturnDonations_thenCancelSpark(){
        sparkService.cancelSparkAndReturnDonations(spark1);

        assertNull(spark1.getCurrentAmount());
        assertEquals(SparkStatus.CANCELLED, spark1.getStatus());
        assertNotNull(spark1.getUpdatedOn());
        verify(sparkRepository).save(spark1);
    }

    @Test
    void givenSparkHasDonations_whenCancelSparkAndReturnDonations_thenCancelSpark(){
        user.setEmail("kalin@gmail.com");
        Wallet wallet = Wallet.builder()
                .owner(user)
                .build();

        Donation donation = Donation.builder()
                .wallet(wallet)
                .amount(BigDecimal.TEN)
                .build();

        spark1.setDonations(List.of(donation));
        spark1.setCurrentAmount(BigDecimal.TEN);

        sparkService.cancelSparkAndReturnDonations(spark1);
        assertEquals(BigDecimal.ZERO, spark1.getCurrentAmount());
        assertEquals(SparkStatus.CANCELLED, spark1.getStatus());
        assertNotNull(spark1.getUpdatedOn());

        verify(walletService).addFundsWithoutUserValidation(any(Wallet.class), any());
        verify(emailService).sendEmail(any(), any(), any());
        verify(sparkRepository).save(spark1);
    }

    @Test
    void givenSparkShouldBeCompleted_whenCompleteSpark_thenCompleteSpark() {
        spark1.setGoalAmount(BigDecimal.TEN);
        sparkService.completeSpark(spark1);

        assertEquals(SparkStatus.COMPLETED, spark1.getStatus());
        assertNotNull(spark1.getUpdatedOn());
        verify(emailService).sendEmail(any(), any(), any());
        verify(sparkRepository).save(spark1);
    }
}
