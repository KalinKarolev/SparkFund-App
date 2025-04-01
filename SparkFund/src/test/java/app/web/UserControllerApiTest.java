package app.web;

import app.security.AuthenticationDetails;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@EnableMethodSecurity
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorizedRequestToSwitchRole_shouldThrowExceptionAndNotFindView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found-error"));
    }

    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).switchRole(any());
    }

    @Test
    void putUnauthorizedRequestToSwitchStatus_shouldThrowExceptionAndNotFindView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/users/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found-error"));
    }

    @Test
    void putAuthorizedRequestToSwitchStatus_shouldRedirectToUsers() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.ADMIN
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/users/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).switchStatus(any());
    }
}
