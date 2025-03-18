package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.web.dto.EditProfileRequest;
import app.web.dto.WalletDonationInfo;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService _userService) {
        userService = _userService;
    }

    @GetMapping("/all")
    public String getAllUsersPage() {
        return "users";
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfilePage(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        WalletDonationInfo walletDonationInfo = userService.getWalletDonationInfo(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DtoMapper.mapUserToEditProfileRequest(user));
        modelAndView.addObject("wallet", user.getWallet());
        modelAndView.addObject("walletDonationInfo", walletDonationInfo);

        return modelAndView;
    }

    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {
        if (userService.isUsernameTaken(editProfileRequest.getUsername(), id)) {
            bindingResult.rejectValue("username", "error.username", "Username is already taken");
        }
        if (userService.isEmailTaken(editProfileRequest.getEmail(), id)) {
            bindingResult.rejectValue("email", "error.email", "Email is already taken");
        }
        if (bindingResult.hasErrors()) {
            User user = userService.getUserById(id);
            WalletDonationInfo walletDonationInfo = userService.getWalletDonationInfo(user);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            modelAndView.addObject("wallet", user.getWallet());
            modelAndView.addObject("walletDonationInfo", walletDonationInfo);

            return modelAndView;
        }

        userService.updateUserProfile(id, editProfileRequest);
        return new ModelAndView("redirect:/home");
    }
}
