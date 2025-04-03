package app.wallet.service;

import app.donation.model.Donation;
import app.exceptions.DomainException;
import app.exceptions.ResourceNotFoundException;
import app.user.model.User;
import app.user.model.UserStatus;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.WalletDonationInfo;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository _walletRepository) {
        walletRepository = _walletRepository;
    }

    @Transactional
    public void createWallet(User user) {
        Wallet wallet = Wallet.builder()
                .owner(user)
                .amount(new BigDecimal("0.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .build();
        walletRepository.save(wallet);
    }

    public WalletDonationInfo getWalletDonationInfo(Wallet wallet) {
        return WalletDonationInfo.builder()
                .totalDonations(getTotalDonations(wallet))
                .totalSparks(getTotalSparkCount(wallet))
                .build();
    }

    /**
     * Calculates the total amount of donations associated with the given wallet.
     * If there are no donations, returns "0.00".
     *
     * @param wallet The wallet whose total donation amount is to be calculated.
     * @return the total donation amount formatted as a string with two decimal places.
     */
    private String getTotalDonations(Wallet wallet) {
        List<Donation> walletDonations = wallet.getDonations();
        if (walletDonations.isEmpty()) {
            return "0.00";
        }
        BigDecimal result = walletDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(result);
    }

    /**
     * Determines the total number of unique Sparks that have received donations
     * from the given wallet. If there are no donations, returns 0.
     *
     * @param wallet The wallet whose donated Sparks are to be counted.
     * @return the number of unique Sparks supported by donations from this wallet.
     */
    private int getTotalSparkCount(Wallet wallet) {
        List<Donation> walletDonations = wallet.getDonations();
        if (walletDonations.isEmpty()) {
            return 0;
        }
        return walletDonations.stream()
                .map(Donation::getSpark)
                .collect(Collectors.toSet())
                .size();
    }

    /**
     * Subtracts the specified amount from the user's wallet balance.
     * If the wallet has sufficient funds, the amount is subtracted,
     * and the wallet balance is updated.
     *
     * @param user The user whose wallet balance is to be reduced.
     * @param amount The amount to be withdrawn from the wallet.
     * @throws DomainException if the wallet balance is insufficient.
     */
    public void reduceAmount(User user, BigDecimal amount) {
        Wallet wallet = user.getWallet();
        BigDecimal walletBalance = wallet.getAmount().subtract(amount);
        if (walletBalance.compareTo(BigDecimal.ZERO) >= 0) {
            wallet.setAmount(walletBalance);
            walletRepository.save(wallet);
        } else {
            throw new DomainException("There is not enough balance in your Wallet for this donation");
        }
    }
    
    public void addFunds(Wallet wallet, BigDecimal amount, UserStatus userStatus) {
        if(userStatus == UserStatus.ACTIVE) {
            addFundsWithoutUserValidation(wallet, amount);
        } else {
            throw new AccessDeniedException("Action denied: Funds cannot be added to the wallet of an inactive user.");
        }
    }

    public void addFundsWithoutUserValidation(Wallet wallet, BigDecimal amount) {
        BigDecimal currentAmount = wallet.getAmount() != null ? wallet.getAmount() : new BigDecimal(0);
        wallet.setAmount(currentAmount.add(amount));
        walletRepository.save(wallet);
    }

    public Wallet findWalletById(UUID _id) {
        return walletRepository.findById(_id)
                .orElseThrow(() -> new ResourceNotFoundException("No wallet with ID [%s] found".formatted(_id)));
    }


    public  List<Wallet> findAllWallets() {
        return walletRepository.findAll();
    }
}
