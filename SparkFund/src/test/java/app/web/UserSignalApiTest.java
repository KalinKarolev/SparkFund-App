package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.service.UserService;
import app.usersignal.model.UserSignal;
import app.usersignal.model.UserSignalStatus;
import app.usersignal.service.UserSignalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserSignalController.class)
@EnableMethodSecurity
public class UserSignalApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserSignalService userSignalService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthorizedRequestToSendSignalPage_thenReturnSignalView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/signal")
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("signal"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userSignalRequest"));
    }

    @Test
    void getAuthorizedRequestToViewSignalPage_thenReturnSignalView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        UUID signalId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/{id}/signal", signalId)
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);
        UserSignal signal = UserSignal.builder()
                .id(signalId)
                .title("Title")
                .message("Message")
                .creator(user)
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        when(userSignalService.getUserSignalById(signalId)).thenReturn(signal);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("signal"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userSignalRequest"));
    }

    @Test
    void getAuthorizedRequestToMySignalsPage_thenReturnAllSignalsView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/user-signals")
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);
        when(userSignalService.getAllSignals(any(), any())).thenReturn(List.of(new UserSignal(), new UserSignal()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("all-signals"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allSignals"))
                .andExpect(model().attributeExists("filterData"));
    }

    @Test
    void getAuthorizedRequestToAllSignalsPage_thenReturnAllSignalsView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/all-signals")
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);
        UserSignal signal1 = UserSignal.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .message("Message")
                .creator(user)
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        UserSignal signal2 = UserSignal.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .message("Message")
                .creator(user)
                .userSignalStatus(UserSignalStatus.PENDING)
                .build();
        when(userSignalService.getAllSignals(any(), any())).thenReturn(List.of(signal1, signal2));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("all-signals"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allSignals"))
                .andExpect(model().attributeExists("filterData"));
    }

    @Test
    void getUnauthorizedRequestToAllSignalsPage_thenThrow404AndReturnErrorPage() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/all-signals")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found-error"));
        verify(userService, never()).getAuthenticatedUser(any());
        verify(userSignalService, never()).getAllSignals(any(), any());
    }

    @Test
    void postAuthorizedRequestToSendSignal_shouldRedirectToHome() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = post("/signals")
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        verify(userSignalService, times(1)).sendSignal(any(), any());
    }

    @Test
    void postInvalidRequestToSendSignal_shouldReturnSignalView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = post("/signals")
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("signal"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userSignalRequest"));
        verify(userSignalService, never()).sendSignal(any(), any());
    }

    @Test
    void whenAuthorizedRequestToCloseSignal_shouldRedirectToAllSignals() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = patch("/signals", UserSignalStatus.PENDING.name())
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message")
                .param("adminResponse", "Admin Response");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("all-signals"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allSignals"))
                .andExpect(model().attributeExists("filterData"));
        verify(userSignalService, times(1)).closeSignal(any(), any());
    }

    @Test
    void whenInvalidRequestToCloseSignal_shouldRedirectToAllSignals() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = patch("/signals", UserSignalStatus.PENDING.name())
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("signal"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userSignalRequest"));
        verify(userSignalService, never()).closeSignal(any(), any());
    }

    @Test
    void whenUnauthorizedRequestToCloseSignal_shouldRedirectToAllSignals() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = patch("/signals", UserSignalStatus.PENDING.name())
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message")
                .param("adminResponse", "Admin Response");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found-error"));
        verify(userSignalService, never()).closeSignal(any(), any());
    }

    @Test
    void whenAuthorizedRequestToDeleteSignal_shouldRedirectToAllSignals() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = delete("/signals", UserSignalStatus.RESOLVED.name())
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allSignals"))
                .andExpect(model().attributeExists("filterData"))
                .andExpect(redirectedUrl("/all-signals"));
        verify(userSignalService, times(1)).deleteSignal(any(), any(), any());
    }

    @Test
    void whenUnauthorizedRequestToDeleteSignal_shouldRedirectToAllSignals() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = delete("/signals", UserSignalStatus.RESOLVED.name())
                .with(user(principal))
                .with(csrf())
                .param("status", UserSignalStatus.PENDING.name())
                .param("title", "Title")
                .param("message", "Message");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found-error"));
        verify(userSignalService, never()).deleteSignal(any(), any(), any());
    }

}
