package app.wallet;

import app.donation.model.Donation;
import app.exceptions.DomainException;
import app.exceptions.ResourceNotFoundException;
import app.spark.model.Spark;
import app.user.model.User;
import app.user.model.UserStatus;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import app.web.dto.WalletDonationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void givenWalletIsCreated_whenCreateWallet_thenCreateWallet() {
        User user = new User();
        walletService.createWallet(user);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet capturedWallet = walletCaptor.getValue();

        assertNotNull(capturedWallet);
        assertEquals(user, capturedWallet.getOwner());
        assertEquals(new BigDecimal("0.00"), capturedWallet.getAmount());
        assertEquals(Currency.getInstance("EUR"), capturedWallet.getCurrency());
        assertNotNull(capturedWallet.getCreatedOn());
    }

    @Test
    void givenRetrieveWalletDonationInfo_whenNoDonations_thenReturnWalletDonationInfo() {
        Wallet wallet = Wallet.builder()
                .donations(new ArrayList<>())
                .build();

        WalletDonationInfo walletDonationInfo = walletService.getWalletDonationInfo(wallet);

        assertEquals("0.00", walletDonationInfo.getTotalDonations());
        assertEquals(0, walletDonationInfo.getTotalSparks());
    }

    @Test
    void givenRetrieveWalletDonationInfo_whenHaveTwoDonationsToTwoSparks_thenReturnWalletDonationInfo() {
        Wallet wallet = Wallet.builder()
                .donations(new ArrayList<>())
                .build();
        Donation donation1 = Donation.builder()
                .wallet(wallet)
                .spark(new Spark())
                .amount(new BigDecimal("100"))
                .build();
        Donation donation2 = Donation.builder()
                .wallet(wallet)
                .spark(new Spark())
                .amount(new BigDecimal("200"))
                .build();
        wallet.setDonations(List.of(donation1, donation2));

        WalletDonationInfo walletDonationInfo = walletService.getWalletDonationInfo(wallet);

        assertEquals("300,00", walletDonationInfo.getTotalDonations());
        assertEquals(2, walletDonationInfo.getTotalSparks());
    }

    @Test
    void givenRetrieveWalletDonationInfo_whenHaveTwoDonationsToOneSparks_thenReturnWalletDonationInfo() {
        Spark spark = new Spark();
        Wallet wallet = Wallet.builder()
                .donations(new ArrayList<>())
                .build();
        Donation donation1 = Donation.builder()
                .wallet(wallet)
                .spark(spark)
                .amount(new BigDecimal("100"))
                .build();
        Donation donation2 = Donation.builder()
                .wallet(wallet)
                .spark(spark)
                .amount(new BigDecimal("200"))
                .build();
        wallet.setDonations(List.of(donation1, donation2));

        WalletDonationInfo walletDonationInfo = walletService.getWalletDonationInfo(wallet);

        assertEquals("300,00", walletDonationInfo.getTotalDonations());
        assertEquals(1, walletDonationInfo.getTotalSparks());
    }

    @Test
    void givenSumIsTakenFromWallet_whenReduceAmount_andAmountIsOverBalance_thenThrowsException() {
        User user = new User();
        Wallet wallet = Wallet.builder()
                .amount(new BigDecimal("100"))
                .build();
        user.setWallet(wallet);

        assertThrows(DomainException.class, () -> walletService.reduceAmount(user, new BigDecimal("101")));
    }

    @Test
    void givenSumIsTakenFromWallet_whenReduceAmount_andAmountEqualsBalance_thenSubtractAmount() {
        User user = new User();
        Wallet wallet = Wallet.builder()
                .amount(new BigDecimal("100"))
                .build();
        user.setWallet(wallet);

        walletService.reduceAmount(user, new BigDecimal("100"));

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet capturedWallet = walletCaptor.getValue();

        assertEquals(BigDecimal.ZERO, capturedWallet.getAmount());
    }

    @Test
    void givenSumIsTakenFromWallet_whenReduceAmount_andAmountIsLessThanBalance_thenSubtractAmount() {
        User user = new User();
        Wallet wallet = Wallet.builder()
                .amount(new BigDecimal("100"))
                .build();
        user.setWallet(wallet);

        walletService.reduceAmount(user, new BigDecimal("60"));

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet capturedWallet = walletCaptor.getValue();

        assertEquals(new BigDecimal("40"), capturedWallet.getAmount());
    }

    @Test
    void givenFundsAreAddedToWallet_andUserIsInactive_thenThrowException() {
        assertThrows(AccessDeniedException.class, () -> walletService.addFunds(new Wallet(), new BigDecimal("100"), UserStatus.INACTIVE));
    }

    @Test
    void givenFundsAreAddedToWallet_andUserIsActive_thenIncreaseWalletAmount() {
        Wallet wallet = Wallet.builder()
                .amount(new BigDecimal("100"))
                .build();

        walletService.addFunds(wallet, new BigDecimal("100"), UserStatus.ACTIVE);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet capturedWallet = walletCaptor.getValue();

        assertEquals(new BigDecimal("200"), capturedWallet.getAmount());
    }

    @Test
    void givenWalletNotExist_whenFindWalletById_thenThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> walletService.findWalletById(UUID.randomUUID()));
    }

    @Test
    void givenTwoExistingWallets_whenFindAllWallets_thenReturnTwoWallets() {
        List<Wallet> wallets = List.of(new Wallet(), new Wallet());

        when(walletRepository.findAll()).thenReturn(wallets);

        List<Wallet> allWallets = walletService.findAllWallets();
        assertEquals(2, allWallets.size());
    }
}
