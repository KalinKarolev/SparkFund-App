package app.web;

import app.security.AuthenticationDetails;
import app.spark.model.Spark;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ManageSparkRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping()
public class SparkController {

    private final UserService userService;
    private final SparkService sparkService;

    public SparkController(UserService _userService, SparkService _sparkService) {
        userService = _userService;
        sparkService = _sparkService;
    }

    @GetMapping("/view-spark")
    public String getSparkPage() {
        return "spark";
    }

    @GetMapping("/manage-spark")
    public ModelAndView getEditSparkPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("manageSparkRequest", new ManageSparkRequest());
        modelAndView.setViewName("manage-spark");

        return modelAndView;
    }

    @PostMapping("/manage-spark")
    public ModelAndView manageSpark(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @Valid ManageSparkRequest manageSparkRequest, BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("manageSparkRequest", manageSparkRequest);
            modelAndView.setViewName("manage-spark");
            return modelAndView;
        }

        sparkService.manageSpark(manageSparkRequest, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("redirect:/home");
        return modelAndView;
    }

    @GetMapping("/all-sparks")
    public ModelAndView getAllSparks(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        List<Spark> activeSparks = sparkService.getAllActiveSparks();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("activeSparks", activeSparks);
        modelAndView.setViewName("all-sparks");

        return modelAndView;
    }
}
