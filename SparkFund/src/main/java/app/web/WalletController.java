package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.EditProfileRequest;
import app.web.dto.WalletDonationInfo;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    public WalletController(WalletService _walletService, UserService _userService) {
        walletService = _walletService;
        userService = _userService;
    }

    @PutMapping("/{id}/add-funds")
    public ModelAndView addFundsToWallet(@PathVariable UUID id
            , @RequestParam(name = "amount") BigDecimal amount
            , @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        Wallet wallet = walletService.findWalletById(id);
        WalletDonationInfo walletDonationInfo = walletService.getWalletDonationInfo(wallet);
        User user = userService.getAuthenticatedUser(authenticationDetails);

        walletService.addFunds(wallet, amount, user.getUserStatus());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DtoMapper.mapUserToEditProfileRequest(user));
        modelAndView.addObject("wallet", user.getWallet());
        modelAndView.addObject("walletDonationInfo", walletDonationInfo);
        modelAndView.setViewName("redirect:/users/" + user.getId() + "/profile");

        return modelAndView;
    }

}
