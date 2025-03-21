package app.web;

import app.security.AuthenticationDetails;
import app.spark.model.Spark;
import app.spark.service.SparkService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ManageSparkRequest;
import app.web.dto.FilterData;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping()
public class SparkController {

    private final UserService userService;
    private final SparkService sparkService;

    public SparkController(UserService _userService, SparkService _sparkService) {
        userService = _userService;
        sparkService = _sparkService;
    }

    @GetMapping("/{id}/show-spark")
    public ModelAndView getSparkPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        Spark spark = sparkService.getSparkById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("spark", spark);
        modelAndView.setViewName("show-spark");

        return modelAndView;
    }

    @GetMapping("/create-spark")
    public ModelAndView getCreateSparkPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("manageSparkRequest", new ManageSparkRequest());
        modelAndView.setViewName("create-spark");

        return modelAndView;
    }

    @PostMapping("/create-spark")
    public ModelAndView createSpark(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @Valid ManageSparkRequest manageSparkRequest, BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("manageSparkRequest", manageSparkRequest);
            modelAndView.setViewName("create-spark");
            return modelAndView;
        }

        Spark spark = sparkService.createSpark(manageSparkRequest, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("redirect:/" + spark.getId() + "/show-spark");
        return modelAndView;
    }

    @GetMapping("/{id}/update-spark")
    public ModelAndView getUpdateSparkPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        Spark sparkForUpdate = sparkService.getSparkById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("manageSparkRequest", DtoMapper.mapSparkToManageSparkRequest(sparkForUpdate));
        modelAndView.setViewName("update-spark");

        return modelAndView;
    }

    @PutMapping("/{id}/update-spark")
    public ModelAndView updateSpark(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails, @Valid ManageSparkRequest manageSparkRequest, BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("manageSparkRequest", manageSparkRequest);
            modelAndView.setViewName("update-spark");
            return modelAndView;
        }

        Spark spark = sparkService.getSparkById(id);
        sparkService.updateSpark(manageSparkRequest, spark, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("redirect:/" + spark.getId() + "/show-spark");
        return modelAndView;
    }

    @GetMapping("/all-sparks")
    public ModelAndView getAllSparks(@AuthenticationPrincipal AuthenticationDetails authenticationDetails
            , @RequestParam(name = "status", required = false) String status
            , @RequestParam(name = "category", required = false) String category
            , @RequestParam(name = "ownership", required = false) String ownership) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        FilterData filterData = new FilterData(status, category, ownership, "all-sparks");
        List<Spark> allSparks = sparkService.getAllSparks(user, status, category, ownership);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSparks", allSparks);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("all-sparks");

        return modelAndView;
    }
}
