package app.usersignal.service;

import app.exceptions.ResourceNotFoundException;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.usersignal.repository.UserSignalRepository;
import app.web.dto.FilterData;
import app.web.dto.UserSignalRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserSignalService {

    private final UserSignalRepository userSignalRepository;

    public UserSignalService(UserSignalRepository _userSignalRepository) {
        userSignalRepository = _userSignalRepository;
    }

    @Transactional
    public ModelAndView sendSignal(UserSignalRequest userSignalRequest, User user) {
        sendSignal(user, userSignalRequest);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("redirect:/home");

        return modelAndView;
    }

    public void sendSignal(User user, UserSignalRequest userSignalRequest) {
        UserSignal userSignal = UserSignal.builder()
                .creator(user)
                .userSignalStatus(UserSignalStatus.PENDING)
                .title(userSignalRequest.getTitle())
                .message(userSignalRequest.getMessage())
                .createdOn(LocalDateTime.now())
                .build();
        userSignalRepository.save(userSignal);
    }

    public UserSignal getUserSignalById(UUID id) {
        return userSignalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user signal found with ID: " + id));
    }

    @Transactional
    public ModelAndView closeSignal(UserSignalRequest userSignalRequest, User user) {
        UserSignal userSignal = getUserSignalById(userSignalRequest.getId());
        closeUserSignal(userSignal, userSignalRequest);
        FilterData filterData = new FilterData("ALL", null, null,"all-signals");

        List<UserSignal> allSignals = getAllSignals(user, "ALL");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("all-signals");

        return modelAndView;
    }

    public void closeUserSignal(UserSignal userSignal, UserSignalRequest userSignalRequest) {
        userSignal.setAdminResponse(userSignalRequest.getAdminResponse());
        userSignal.setUserSignalStatus(UserSignalStatus.RESOLVED);
        userSignal.setUpdatedOn(LocalDateTime.now());
        userSignalRepository.save(userSignal);
    }

    @Transactional
    public ModelAndView deleteSignal(UserSignalRequest userSignalRequest, User user, String status) throws AccessDeniedException {
        UserSignal signal = getUserSignalById(userSignalRequest.getId());
        if (UserSignalStatus.RESOLVED != signal.getUserSignalStatus()) {
            throw new AccessDeniedException("Only signals in status 'Resolved' can be deleted");
        }
        userSignalRepository.delete(signal);

        List<UserSignal> allSignals = getAllSignals(user, status);
        FilterData filterData = new FilterData(status, null, null, "all-signals");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSignals", allSignals);
        modelAndView.addObject("filterData", filterData);
        modelAndView.setViewName("redirect:/all-signals");

        return modelAndView;
    }

    public List<UserSignal> getAllSignals(User user, String status) {
        List<UserSignal> allSignals = userSignalRepository.findAll();
        allSignals.removeIf(userSignal -> userSignal.getCreator().getId().equals(user.getId()));
        if ("ALL".equals(status)) {
            return allSignals;
        }
        allSignals.removeIf(userSignal -> !userSignal.getUserSignalStatus().name().equals(status));
        return allSignals;
    }

    public List<UserSignal> getUserSignals(List<UserSignal> userSignals, String status) {
        if ("ALL".equals(status)) {
            return userSignals;
        }
        userSignals.removeIf(userSignal -> !userSignal.getUserSignalStatus().name().equals(status));
        return userSignals;
    }
}
