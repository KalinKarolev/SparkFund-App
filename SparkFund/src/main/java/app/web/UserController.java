package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.dto.WalletDonationInfo;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService _userService) {
        userService = _userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfilePage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        if (!authenticationDetails.getUserId().equals(id)) {
            throw new AuthorizationDeniedException("You are not authorized to view or edit this profile.");
        }
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsersPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        List<User> allUsers = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allUsers", allUsers);
        modelAndView.setViewName("users");

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserStatus(@PathVariable UUID id) {
        userService.switchStatus(id);
        return "redirect:/users";
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserRole(@PathVariable UUID id) {
        userService.switchRole(id);
        return "redirect:/users";
    }
}
