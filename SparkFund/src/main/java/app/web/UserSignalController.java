package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.usersignal.model.UserSignal;
import app.usersignal.service.UserSignalService;
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

import java.nio.file.AccessDeniedException;
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

    @PostMapping("/signal/actions")
    public ModelAndView handleSignalAction(@AuthenticationPrincipal AuthenticationDetails authenticationDetails
            , @RequestParam("actionType") String actionType
            , @RequestParam(name = "status", required = false, defaultValue = "ALL") String status
            , @Valid UserSignalRequest userSignalRequest
            , BindingResult bindingResult) throws AccessDeniedException {

        User user = userService.getAuthenticatedUser(authenticationDetails);
        if ("close".equals(actionType) && userSignalRequest.getAdminResponse() == null) {
            bindingResult.rejectValue("adminResponse", "error.adminResponse", "You cannot close signal without response");
        }
        if ("send".equals(actionType) && bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("userSignalRequest", userSignalRequest);
            modelAndView.setViewName("signal");
            return modelAndView;
        }

        userSignalService.validateActionType(actionType);

        if ("send".equals(actionType)) {
            userSignalService.sendSignal(user, userSignalRequest);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.setViewName("redirect:/home");
            return modelAndView;
        } else if ("close".equals(actionType)) {
            List<UserSignal> allSignals = userSignalService.closeSignal(userSignalRequest, user);
            FilterData filterData = new FilterData("ALL", null, null,"all-signals");

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("allSignals", allSignals);
            modelAndView.addObject("filterData", filterData);
            modelAndView.setViewName("all-signals");
            return modelAndView;
        } else {
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

}
