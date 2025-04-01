package app.web;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User randomUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .userRole(UserRole.USER)
                .build();
    }
}
