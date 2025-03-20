package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.usersignal.model.UserSignal;
import app.usersignal.service.UserSignalService;
import app.web.dto.UserSignalRequest;
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
public class UserSignalController {

    private final UserService userService;
    private final UserSignalService userSignalService;

    public UserSignalController(UserService _userService, UserSignalService _userSignalService) {
        userService = _userService;
        userSignalService = _userSignalService;
    }

    @GetMapping("/send-signal")
    public ModelAndView getSendSignalPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userSignalRequest", new UserSignalRequest());
        modelAndView.setViewName("signal");

        return modelAndView;
    }

    @PostMapping("/send-signal")
    public ModelAndView sendSignal(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @Valid UserSignalRequest userSignalRequest, BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("userSignalRequest", userSignalRequest);
            modelAndView.setViewName("signal");
            return modelAndView;
        }
        userSignalService.sendSignal(user, userSignalRequest);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("redirect:/home");

        return modelAndView;
    }

    @PutMapping("/close-signal")
    public ModelAndView closeSignal(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, @Valid UserSignalRequest userSignalRequest, BindingResult bindingResult) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        if (userSignalRequest.getAdminResponse() == null) {
            bindingResult.rejectValue("adminResponse", "error.adminResponse", "You cannot close signal without response");
        }
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("userSignalRequest", userSignalRequest);
            modelAndView.setViewName("signal");
            return modelAndView;
        }
        UserSignal userSignal = userSignalService.getUserSignalById(userSignalRequest.getId());
        userSignalService.closeUserSignal(userSignal, userSignalRequest);

        List<UserSignal> allSignals = userSignalService.getAllSignals(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.setViewName("all-signals");

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

    @GetMapping("/my-signals")
    public ModelAndView getMySignalsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", user.getUserSignals());
        modelAndView.setViewName("all-signals");

        return modelAndView;
    }

    @GetMapping("/all-signals")
    public ModelAndView getAllSignalsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getAuthenticatedUser(authenticationDetails);
        List<UserSignal> allSignals = userSignalService.getAllSignals(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.setViewName("all-signals");

        return modelAndView;
    }
}
