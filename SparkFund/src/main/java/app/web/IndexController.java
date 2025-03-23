package app.web;

import app.donation.service.DonationService;
import app.security.AuthenticationDetails;
import app.spark.model.Spark;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.TotalDonationsInfo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping
public class IndexController {

    private final UserService userService;
    private final SparkService sparkService;
    private final DonationService donationService;

    public IndexController(UserService _userService, SparkService _sparkService, DonationService _donationService) {
        userService = _userService;
        sparkService = _sparkService;
        donationService = _donationService;
    }

    @GetMapping
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("registerRequest", new RegisterRequest());
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (!Objects.equals(registerRequest.getPassword(), registerRequest.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        User registeredUser = userService.register(registerRequest);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", registeredUser);
        modelAndView.setViewName("redirect:/login");
        return modelAndView;
    }


    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("loginRequest", new LoginRequest());
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User registeredUser = userService.getAuthenticatedUser(authenticationDetails);

        List<Spark> allActiveSparks = sparkService.getAllActiveSparks();
        TotalDonationsInfo donationsInfo = donationService.getTotalDonationsInfo();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", registeredUser);
        modelAndView.addObject("allSparks", allActiveSparks);
        modelAndView.addObject("donationsInfo", donationsInfo);
        modelAndView.setViewName("home");
        return modelAndView;
    }
}
