package app.usersignal.service;

import app.exceptions.DomainException;
import app.exceptions.ResourceNotFoundException;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.usersignal.repository.UserSignalRepository;
import app.web.dto.EmailEvent;
import app.web.dto.UserSignalRequest;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserSignalService {

    private final UserSignalRepository userSignalRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserSignalService(UserSignalRepository _userSignalRepository, ApplicationEventPublisher _eventPublisher) {
        userSignalRepository = _userSignalRepository;
        eventPublisher = _eventPublisher;
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

    @Transactional
    public List<UserSignal> closeSignal(UserSignalRequest userSignalRequest, User user) {
        UserSignal userSignal = getUserSignalById(userSignalRequest.getId());
        closeUserSignal(userSignal, userSignalRequest);
        sendEmailForClosedSignal(userSignal, userSignalRequest.getAdminResponse());

        return getAllSignals(user, "ALL");
    }

    private void closeUserSignal(UserSignal userSignal, UserSignalRequest userSignalRequest) {
        userSignal.setAdminResponse(userSignalRequest.getAdminResponse());
        userSignal.setUserSignalStatus(UserSignalStatus.RESOLVED);
        userSignal.setUpdatedOn(LocalDateTime.now());
        userSignalRepository.save(userSignal);
    }

    private void sendEmailForClosedSignal(UserSignal userSignal, String adminResponse) {
        EmailEvent emailEvent = EmailEvent.builder()
                .userEmail(userSignal.getCreator().getEmail())
                .signalTitle(userSignal.getTitle())
                .adminResponse(adminResponse)
                .build();
        eventPublisher.publishEvent(emailEvent);
    }

    @Transactional
    public List<UserSignal> deleteSignal(UserSignalRequest userSignalRequest, User user, String filterStatus) {
        UserSignal signal = getUserSignalById(userSignalRequest.getId());
        if (UserSignalStatus.RESOLVED != signal.getUserSignalStatus()) {
            throw new AccessDeniedException("Only signals in status 'Resolved' can be deleted");
        }
        userSignalRepository.delete(signal);

        return getAllSignals(user, filterStatus);
    }

    public List<UserSignal> getAllSignals(User user, String status) {
        List<UserSignal> allSignals = userSignalRepository.findAllByOrderByCreatedOnDesc();
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

    public void validateActionType(String actionType) {
        Set<String> allowedActionTypes = Set.of("send", "close", "delete");
        if (!allowedActionTypes.contains(actionType)) {
            throw new DomainException("Unsupported action type: " + actionType);
        }
    }

    public UserSignal getUserSignalById(UUID id) {
        return userSignalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user signal found with ID: " + id));
    }
}
