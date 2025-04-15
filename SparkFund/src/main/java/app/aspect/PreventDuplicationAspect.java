package app.aspect;

import app.exceptions.DuplicateException;
import app.security.AuthenticationDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class PreventDuplicationAspect {

    private final Map<String, Long> submissionCache = new ConcurrentHashMap<>();

    @Around(value = "@annotation(org.springframework.web.bind.annotation.PostMapping) && within(app.web.DonationController) && args(sparkId, authenticationDetails, ..)", argNames = "joinPoint,sparkId,authenticationDetails")
    public Object preventDonationDuplication(ProceedingJoinPoint joinPoint, UUID sparkId, AuthenticationDetails authenticationDetails) throws Throwable {
        String invokedMethodName = joinPoint.getSignature().getName();
        String donationUniqueKey = invokedMethodName + ";" + sparkId + ";" + authenticationDetails.getUserId();

        long now = System.currentTimeMillis();
        long timeoutMillis = 5000L;

        if (submissionCache.containsKey(donationUniqueKey)) {
            long lastTime = submissionCache.get(donationUniqueKey);
            if (now - lastTime < timeoutMillis) {
                throw new DuplicateException("Duplicate donation attempt detected.");
            }
        }

        submissionCache.put(donationUniqueKey, now);

        try {
            return joinPoint.proceed();
        } finally {
            submissionCache.remove(donationUniqueKey);
        }
    }
}
