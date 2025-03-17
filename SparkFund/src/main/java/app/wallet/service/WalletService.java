package app.wallet.service;

import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository _walletRepository) {
        walletRepository = _walletRepository;
    }

    @Transactional
    public Wallet createWallet(User user) {
        Wallet wallet = Wallet.builder()
                .owner(user)
                .amount(new BigDecimal("0.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .build();
        return walletRepository.save(wallet);
    }
}
