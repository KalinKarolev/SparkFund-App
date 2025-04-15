package app.aspect;

import app.exceptions.DuplicateException;
import app.security.AuthenticationDetails;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PreventDuplicationAspectUTest {

    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private Signature signature;

    @InjectMocks
    private PreventDuplicationAspect aspect;

    @BeforeEach
    void setup() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("donateToSpark");
    }

    @Test
    void shouldProcessDonationRequest_whenNoDuplicateDonationIsDetected() throws Throwable {
        when(joinPoint.proceed()).thenReturn("OK");
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID()
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);
        Object result = aspect.preventDonationDuplication(joinPoint, UUID.randomUUID(), principal);
        assertEquals("OK", result);
    }

    @Test
    void shouldThrowDuplicateException_whenDuplicateDonationIsDetected() throws Throwable {
        UUID sparkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Field cacheField = PreventDuplicationAspect.class.getDeclaredField("submissionCache");
        cacheField.setAccessible(true);
        String key = "donateToSpark" + ";" + sparkId + ";" + userId;
        @SuppressWarnings("unchecked")
        Map<String, Long> cache = (Map<String, Long>) cacheField.get(aspect);
        cache.put(key, System.currentTimeMillis());

        AuthenticationDetails principal = new AuthenticationDetails(userId
                , "Kalin"
                , "12345"
                , UserRole.USER
                , UserStatus.ACTIVE);

        DuplicateException exception = assertThrows(DuplicateException.class, () ->
                aspect.preventDonationDuplication(joinPoint, sparkId, principal)
        );
        assertEquals("Duplicate donation attempt detected.", exception.getMessage());
    }
}
