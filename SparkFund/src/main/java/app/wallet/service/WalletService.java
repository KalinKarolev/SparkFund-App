package app.wallet.service;

import app.donation.model.Donation;
import app.exceptions.DomainException;
import app.user.model.User;
import app.user.model.UserStatus;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.WalletDonationInfo;
import jakarta.transaction.Transactional;
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

    public void reduceAmount(User user, BigDecimal amount) {
        Wallet wallet = user.getWallet();
        BigDecimal walletBalance = wallet.getAmount().subtract(amount);
        if (walletBalance.compareTo(BigDecimal.ZERO) > 0) {
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
            throw new DomainException("Action denied: Funds cannot be added to the wallet of an inactive user.");
        }
    }

    public void addFundsWithoutUserValidation(Wallet wallet, BigDecimal amount) {
        BigDecimal currentAmount = wallet.getAmount() != null ? wallet.getAmount() : new BigDecimal(0);
        wallet.setAmount(currentAmount.add(amount));
        walletRepository.save(wallet);
    }

    public Wallet findWalletById(UUID _id) {
        return walletRepository.findById(_id)
                .orElseThrow(() -> new DomainException("No wallet with ID [%s] found".formatted(_id)));
    }


}
