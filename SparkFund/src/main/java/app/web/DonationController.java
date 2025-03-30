package app.web;

import app.donation.service.DonationService;
import app.security.AuthenticationDetails;
import app.spark.model.Spark;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.DonationRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Controller
@RequestMapping()
public class DonationController {

    private final DonationService donationService;
    private final UserService userService;
    private final SparkService sparkService;

    public DonationController(DonationService _donationService, UserService _userService, SparkService _sparkService) {
        donationService = _donationService;
        userService = _userService;
        sparkService = _sparkService;
    }

    @GetMapping("/{id}/donation")
    public ModelAndView getDonationPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        Spark spark = sparkService.getSparkById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("spark", spark);
        modelAndView.addObject("donationRequest", new DonationRequest());
        modelAndView.setViewName("donation");

        return modelAndView;
    }

    @PostMapping("/{id}/donation")
    public ModelAndView donateToSpark(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationDetails authenticationDetails
            , @Valid DonationRequest donationRequest
            , BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        Spark spark = sparkService.getSparkById(id);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("spark", spark);
            modelAndView.addObject("donationRequest", donationRequest);
            modelAndView.setViewName("donation");
            return modelAndView;
        }

        donationService.addDonationToSpark(user, spark, donationRequest);
        donationService.sendEmailForDonation(spark, user.getUsername(), donationRequest.getMessage(), donationRequest.getAmount());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("spark", spark);
        modelAndView.setViewName("redirect:/" + spark.getId() + "/spark");
        return modelAndView;
    }
}
