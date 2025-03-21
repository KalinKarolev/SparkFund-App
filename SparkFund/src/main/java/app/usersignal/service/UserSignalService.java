package app.usersignal.service;

import app.exceptions.DomainException;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.usersignal.repository.UserSignalRepository;
import app.web.dto.UserSignalRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new DomainException("No user signal found with ID: " + id));
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

    @Transactional
    public void closeUserSignal(UserSignal userSignal, UserSignalRequest userSignalRequest) {
        userSignal.setAdminResponse(userSignalRequest.getAdminResponse());
        userSignal.setUserSignalStatus(UserSignalStatus.RESOLVED);
        userSignal.setUpdatedOn(LocalDateTime.now());
        userSignalRepository.save(userSignal);
    }
}
