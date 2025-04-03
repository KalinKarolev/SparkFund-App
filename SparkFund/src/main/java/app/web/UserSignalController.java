package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.usersignal.model.UserSignal;
import app.usersignal.service.UserSignalService;
import app.util.CommonUtils;
import app.web.dto.FilterData;
import app.web.dto.UserSignalRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping()
public class UserSignalController {

    private final UserService userService;
    private final UserSignalService userSignalService;

    public UserSignalController(UserService _userService, UserSignalService _userSignalService) {
        userService = _userService;
        userSignalService = _userSignalService;
    }

    @GetMapping("/signal")
    public ModelAndView getSendSignalPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userSignalRequest", new UserSignalRequest());
        modelAndView.setViewName("signal");

        return modelAndView;
    }

    @GetMapping("/{id}/signal")
    public ModelAndView getViewSignalPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @PathVariable UUID id) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        UserSignal signal = userSignalService.getUserSignalById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userSignalRequest", DtoMapper.mapUserSignalToUserSignalRequest(signal));
        modelAndView.setViewName("signal");

        return modelAndView;
    }

    @GetMapping("/user-signals")
    public ModelAndView getMySignalsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @RequestParam(name = "status", required = false, defaultValue = "ALL") String status) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        FilterData filterData = new FilterData(status, null, null, "user-signals");

        List<UserSignal> userSignals = userSignalService.getUserSignals(user.getUserSignals(), status);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", userSignals);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("all-signals");

        return modelAndView;
    }

    @GetMapping("/all-signals")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllSignalsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @RequestParam(name = "status", required = false, defaultValue = "ALL") String status) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        List<UserSignal> allSignals = userSignalService.getAllSignals(user, status);
        FilterData filterData = new FilterData(status, null, null, "all-signals");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("all-signals");

        return modelAndView;
    }

    @PostMapping("/signals")
    public ModelAndView sendSignal(@AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                   @Valid UserSignalRequest userSignalRequest,
                                   BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("signal");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userSignalRequest", userSignalRequest);
            return modelAndView;
        }

        userSignalService.sendSignal(user, userSignalRequest);
        return new ModelAndView("redirect:/home");
    }

    @PatchMapping("/signals")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView closeSignal(@AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                    @Valid UserSignalRequest userSignalRequest,
                                    BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        if (CommonUtils.isEmpty(userSignalRequest.getAdminResponse())) {
            bindingResult.rejectValue("adminResponse", "error.adminResponse", "You cannot close signal without response");
        }
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("userSignalRequest", userSignalRequest);
            modelAndView.setViewName("signal");
            return modelAndView;
        }

        List<UserSignal> allSignals = userSignalService.closeSignal(userSignalRequest, user);
        FilterData filterData = new FilterData("ALL", null, null, "all-signals");

        ModelAndView modelAndView = new ModelAndView("all-signals");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.addObject("filterData", filterData);
        return modelAndView;
    }

    @DeleteMapping("/signals")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteSignal(@AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                     @RequestParam(name = "status", required = false, defaultValue = "ALL") String status,
                                     @Valid UserSignalRequest userSignalRequest) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        List<UserSignal> allSignals = userSignalService.deleteSignal(userSignalRequest, user, status);
        FilterData filterData = new FilterData(status, null, null, "all-signals");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("redirect:/all-signals");
        return modelAndView;
    }

}
