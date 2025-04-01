package app.web;

import app.donation.service.DonationService;
import app.exceptions.EmailAlreadyExistException;
import app.exceptions.UsernameAlreadyExistException;
import app.security.AuthenticationDetails;
import app.spark.service.SparkService;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.service.UserService;
import app.web.dto.TotalDonationsInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@AutoConfigureMockMvc(addFilters = false)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private SparkService sparkService;
    @MockitoBean
    private DonationService donationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithError_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {
        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kalin")
                .formField("email", "kalin@gmail.com")
                .formField("password", "12345")
                .formField("confirmPassword", "12345")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegister_whenEmailAlreadyExists_thenRedirectToRegisterWithFlashParam() throws Exception {
        when(userService.register(any())).thenThrow(new EmailAlreadyExistException("Email already exists"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kalin")
                .formField("email", "kalin@gmail.com")
                .formField("password", "12345")
                .formField("confirmPassword", "12345")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("emailAlreadyExistMessage"));
    }

    @Test
    void postRequestToRegister_whenUsernameAlreadyExists_thenRedirectToRegister() throws Exception {
        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exists"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kalin")
                .formField("email", "kalin@gmail.com")
                .formField("password", "12345")
                .formField("confirmPassword", "12345")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"));
    }

    @Test
    void postRequestToRegisterEndpointWithNotMatchingPasswords_returnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kalin")
                .formField("email", "kalin@gmail.com")
                .formField("password", "12345")
                .formField("confirmPassword", "67890")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kalin")
                .formField("email", "")
                .formField("password", "12345")
                .formField("confirmPassword", "12345")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal))
                .with(csrf());

        when(userService.getAuthenticatedUser(any())).thenReturn(TestBuilder.randomUser());

        TotalDonationsInfo mockDonationsInfo = new TotalDonationsInfo();
        mockDonationsInfo.setTotalMoneyRaised(new BigDecimal("1234.56"));
        when(donationService.getTotalDonationsInfo()).thenReturn(mockDonationsInfo);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allSparks"))
                .andExpect(model().attributeExists("donationsInfo"));
        verify(userService, times(1)).getAuthenticatedUser(any());
    }

}
