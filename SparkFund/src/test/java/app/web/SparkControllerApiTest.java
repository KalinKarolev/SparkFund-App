package app.web;

import app.security.AuthenticationDetails;
import app.spark.model.Spark;
import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import app.spark.service.SparkService;
import app.user.model.User;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SparkController.class)
@EnableMethodSecurity
public class SparkControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private SparkService sparkService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthorizedRequestToGetSparkPage_thenReturnShowSparkView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = get("/{id}/spark", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Spark spark = Spark.builder()
                .id(UUID.randomUUID())
                .creator(user)
                .currentAmount(BigDecimal.ZERO)
                .goalAmount(BigDecimal.TEN)
                .build();
        when(userService.getAuthenticatedUser(any())).thenReturn(user);
        when(sparkService.getSparkById(any())).thenReturn(spark);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "spark"))
                .andExpect(view().name("show-spark"));
    }

    @Test
    void postAuthorizedRequestToCreateSpark_thenRedirectToSparkPage() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = post("/spark/new")
                .with(user(principal))
                .with(csrf())
                .param("title", "Test Spark")
                .param("description", "Spark Title That Aims for 50 symbols length to test")
                .param("firstPictureUrl", "https://www.img.com")
                .param("status", SparkStatus.ACTIVE.name())
                .param("category", SparkCategory.EDUCATION.name())
                .param("currentAmount", BigDecimal.ZERO.toString())
                .param("goalAmount", BigDecimal.TEN.toString());

        Spark spark = Spark.builder()
                .id(UUID.randomUUID())
                .creator(new User())
                .currentAmount(BigDecimal.ZERO)
                .goalAmount(BigDecimal.TEN)
                .build();
        when(sparkService.createSpark(any(), any())).thenReturn(spark);
        when(userService.getAuthenticatedUser(any())).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("user"))
                .andExpect(redirectedUrl("/" + spark.getId() + "/spark"));
        verify(sparkService, times(1)).createSpark(any(), any());
    }

    @Test
    void postInvalidRequestToCreateSpark_thenReturnCreateSparkView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = post("/spark/new")
                .with(user(principal))
                .with(csrf())
                .param("title", "")
                .param("description", "")
                .param("firstPictureUrl", "")
                .param("status", SparkStatus.ACTIVE.name())
                .param("category", SparkCategory.EDUCATION.name())
                .param("currentAmount", BigDecimal.ZERO.toString())
                .param("goalAmount", BigDecimal.TEN.toString());

        when(userService.getAuthenticatedUser(any())).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "manageSparkRequest"))
                .andExpect(view().name("create-spark"));
        verify(sparkService, never()).createSpark(any(), any());
    }

    @Test
    void putAuthorizedRequestToUpdateSpark_thenRedirectToSparkPage() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/{id}/spark/details", UUID.randomUUID())
                .with(user(principal))
                .with(csrf())
                .param("title", "Test Spark")
                .param("description", "Spark Title That Aims for 50 symbols length to test")
                .param("firstPictureUrl", "https://www.img.com")
                .param("status", SparkStatus.ACTIVE.name())
                .param("category", SparkCategory.EDUCATION.name())
                .param("currentAmount", BigDecimal.ZERO.toString())
                .param("goalAmount", BigDecimal.TEN.toString());

        Spark spark = Spark.builder()
                .id(UUID.randomUUID())
                .creator(new User())
                .currentAmount(BigDecimal.ZERO)
                .goalAmount(BigDecimal.TEN)
                .build();
        when(sparkService.getSparkById(any())).thenReturn(spark);
        when(userService.getAuthenticatedUser(any())).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("user"))
                .andExpect(redirectedUrl("/" + spark.getId() + "/spark"));
        verify(sparkService, times(1)).updateSpark(any(), any(), any());
    }

    @Test
    void putInvalidRequestToUpdateSpark_thenReturnCreateSparkView() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/{id}/spark/details", UUID.randomUUID())
                .with(user(principal))
                .with(csrf())
                .param("title", "")
                .param("description", "")
                .param("firstPictureUrl", "")
                .param("status", SparkStatus.ACTIVE.name())
                .param("category", SparkCategory.EDUCATION.name())
                .param("currentAmount", BigDecimal.ZERO.toString())
                .param("goalAmount", BigDecimal.TEN.toString());

        when(userService.getAuthenticatedUser(any())).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "manageSparkRequest"))
                .andExpect(view().name("update-spark"));
        verify(sparkService, never()).updateSpark(any(), any(), any());
    }

    @Test
    void putAuthorizedRequestToCancelSpark_thenRedirectToSparkPage() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/{id}/spark/cancelled", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        Spark spark = Spark.builder()
                .id(UUID.randomUUID())
                .build();
        when(sparkService.getSparkById(any())).thenReturn(spark);
        when(userService.getAuthenticatedUser(any())).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("user"))
                .andExpect(redirectedUrl("/" + spark.getId() + "/spark"));
        verify(sparkService, times(1)).cancelSparkAndReturnDonations(any());
    }

    @Test
    void givenRedirectToNonExistingSpark_thenThrowErrorAndShowErrorPage() throws Exception {
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        MockHttpServletRequestBuilder request = put("/{id}/spark/details", UUID.randomUUID())
                .with(user(principal))
                .with(csrf())
                .param("title", "Test Spark")
                .param("description", "Spark Title That Aims for 50 symbols length to test")
                .param("firstPictureUrl", "https://www.img.com")
                .param("status", SparkStatus.ACTIVE.name())
                .param("category", SparkCategory.EDUCATION.name())
                .param("currentAmount", BigDecimal.ZERO.toString())
                .param("goalAmount", BigDecimal.TEN.toString());

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("generic-error"))
                .andExpect(model().attributeExists("exceptionName"))
                .andExpect(model().attribute("exceptionName", "NullPointerException"));
    }

}
