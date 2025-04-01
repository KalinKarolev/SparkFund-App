package app.usersignal;

import app.exceptions.DomainException;
import app.exceptions.ResourceNotFoundException;
import app.user.model.User;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.usersignal.repository.UserSignalRepository;
import app.usersignal.service.UserSignalService;
import app.web.dto.UserSignalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserSignalServiceUTest {

    @Mock
    private UserSignalRepository userSignalRepository;

    @InjectMocks
    private UserSignalService userSignalService;

    @Test
    void givenSignalIsSent_whenSendSignal_thenCreateSignal() {
        User user = new User();
        UserSignalRequest userSignalRequest = UserSignalRequest.builder()
                .title("Title")
                .message("Message")
                .build();

        userSignalService.sendSignal(user, userSignalRequest);

        ArgumentCaptor<UserSignal> userSignalCaptor = ArgumentCaptor.forClass(UserSignal.class);
        verify(userSignalRepository).save(userSignalCaptor.capture());
        UserSignal userSignal = userSignalCaptor.getValue();

        assertEquals(user, userSignal.getCreator());
        assertEquals(UserSignalStatus.PENDING, userSignal.getUserSignalStatus());
        assertEquals(userSignalRequest.getTitle(), userSignal.getTitle());
        assertEquals(userSignalRequest.getMessage(), userSignal.getMessage());
    }

    @Test
    void givenSignalNotExists_whenCloseSignal_thenThrowException() {
        when(userSignalRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userSignalService.closeSignal(new UserSignalRequest(), new User()));
    }

    @Test
    void givenSignalExists_whenCloseSignal_thenCloseSignal() {
        UUID uuid = UUID.randomUUID();
        UserSignal userSignal = UserSignal.builder()
                .id(uuid)
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        UserSignalRequest signalRequest = UserSignalRequest.builder()
                .id(uuid)
                .adminResponse("message")
                .build();

        when(userSignalRepository.findById(uuid)).thenReturn(Optional.of(userSignal));

        userSignalService.closeSignal(signalRequest, new User());

        assertEquals(UserSignalStatus.RESOLVED, userSignal.getUserSignalStatus());
        assertEquals("message", userSignal.getAdminResponse());
        assertNotNull(userSignal.getUpdatedOn());
    }

    @Test
    void givenSignalIsDeleted_whenSignalNotResolved_thenThrowException() {
        UUID uuid = UUID.randomUUID();
        UserSignal userSignal = UserSignal.builder()
                .id(uuid)
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        UserSignalRequest signalRequest = UserSignalRequest.builder()
                .id(uuid)
                .adminResponse("message")
                .build();

        when(userSignalRepository.findById(uuid)).thenReturn(Optional.of(userSignal));

        assertThrows(AccessDeniedException.class, () -> userSignalService.deleteSignal(signalRequest, new User(), "ALL"));
    }

    @Test
    void givenSignalIsDeleted_whenSignalResolved_thenExecuteDelete() {
        UUID uuid = UUID.randomUUID();
        UserSignal userSignal = UserSignal.builder()
                .id(uuid)
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        UserSignalRequest signalRequest = UserSignalRequest.builder()
                .id(uuid)
                .adminResponse("message")
                .build();

        when(userSignalRepository.findById(uuid)).thenReturn(Optional.of(userSignal));

        userSignalService.deleteSignal(signalRequest, new User(), "ALL");
        verify(userSignalRepository).delete(userSignal);
    }

    @Test
    void givenAllUsersSignalsRequired_whenGetAllSignals_thenGetAllSignalsExceptCurrentUserSignals() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        UserSignal userSignal1 = UserSignal.builder()
                .creator(user)
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        UserSignal userSignal2 = UserSignal.builder()
                .creator(User.builder()
                        .id(UUID.randomUUID())
                        .build())
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        List<UserSignal> allSignals = new ArrayList<>(Arrays.asList(userSignal1, userSignal2));

        when(userSignalRepository.findAllByOrderByCreatedOnDesc()).thenReturn(allSignals);

        List<UserSignal> retrievedSignals = userSignalService.getAllSignals(user, "ALL");
        assertEquals(1, retrievedSignals.size());
        assertNotEquals(user, retrievedSignals.get(0).getCreator());
    }

    @Test
    void givenPendingUsersSignalsRequired_whenGetAllSignals_thenGetAllPendingSignals() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        UserSignal userSignal1 = UserSignal.builder()
                .creator(User.builder()
                        .id(UUID.randomUUID())
                        .build())
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        UserSignal userSignal2 = UserSignal.builder()
                .creator(User.builder()
                        .id(UUID.randomUUID())
                        .build())
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        List<UserSignal> allSignals = new ArrayList<>(Arrays.asList(userSignal1, userSignal2));

        when(userSignalRepository.findAllByOrderByCreatedOnDesc()).thenReturn(allSignals);

        List<UserSignal> retrievedSignals = userSignalService.getAllSignals(user, "PENDING");
        assertEquals(1, retrievedSignals.size());
        assertEquals(UserSignalStatus.PENDING, retrievedSignals.get(0).getUserSignalStatus());
    }

    @Test
    void givenAllUserSignalsRequired_whenGetUserSignals_thenGetAllUserSignals() {
        UserSignal userSignal1 = UserSignal.builder()
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        UserSignal userSignal2 = UserSignal.builder()
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        List<UserSignal> userSignals = new ArrayList<>(Arrays.asList(userSignal1, userSignal2));

        List<UserSignal> retrievedSignals = userSignalService.getUserSignals(userSignals, "ALL");
        assertEquals(2, retrievedSignals.size());
    }

    @Test
    void givenPendingUserSignalsRequired_whenGetUserSignals_thenGetPendingUserSignals() {
        UserSignal userSignal1 = UserSignal.builder()
                .userSignalStatus(UserSignalStatus.RESOLVED)
                .build();
        UserSignal userSignal2 = UserSignal.builder()
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        List<UserSignal> userSignals = new ArrayList<>(Arrays.asList(userSignal1, userSignal2));

        List<UserSignal> retrievedSignals = userSignalService.getUserSignals(userSignals, "PENDING");
        assertEquals(1, retrievedSignals.size());
        assertEquals(UserSignalStatus.PENDING, retrievedSignals.get(0).getUserSignalStatus());
    }

    @Test
    void givenInvalidActionType_whenValidateActionType_thenThrowException() {
        assertThrows(DomainException.class, () -> userSignalService.validateActionType("invalidActionType"));
    }

    @Test
    void givenValidActionType_whenValidateActionType_thenDoesNotThrowException() {
        assertDoesNotThrow(() -> userSignalService.validateActionType("send"));
        assertDoesNotThrow(() -> userSignalService.validateActionType("close"));
        assertDoesNotThrow(() -> userSignalService.validateActionType("delete"));
    }

}
