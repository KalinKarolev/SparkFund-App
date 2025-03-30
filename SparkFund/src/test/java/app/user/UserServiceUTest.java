package app.user;

import app.exceptions.EmailAlreadyExistException;
import app.exceptions.ResourceNotFoundException;
import app.exceptions.UsernameAlreadyExistException;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.WalletDonationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private UserService userService;

    @Test
    void givenUsernameExists_whenRegisterUser_thenThrowsException() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(RegisterRequest.builder().username(any()).build()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenUsernameIsNew_andEmailExists_whenRegisterUser_thenThrowsException() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.register(RegisterRequest.builder().username(any()).build()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenUserIsCreatedAndSaved() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Kalin")
                .email("kalin@gmail.com")
                .password("password")
                .confirmPassword("password")
                .build();
        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encodedPassword")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User createdUser = userService.register(request);

        assertNotNull(createdUser);
        assertEquals("Kalin", createdUser.getUsername());
        assertEquals("kalin@gmail.com", createdUser.getEmail());
        assertEquals(UserRole.USER, createdUser.getUserRole());
        assertEquals(UserStatus.ACTIVE, createdUser.getUserStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("Kalin", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("kalin@gmail.com", savedUser.getEmail());
        verify(walletService).createWallet(any(User.class));
    }

    @Test
    void givenUserNotExists_whenLoadUserByUsername_thenThrowsException() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.loadUserByUsername(any()));
    }

    @Test
    void givenUserExists_whenLoadUserByUsername_thenReturnUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Kalin")
                .password("password")
                .userRole(UserRole.ADMIN)
                .userStatus(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

        assertInstanceOf(AuthenticationDetails.class, userDetails);
        AuthenticationDetails authenticationDetails = (AuthenticationDetails) userDetails;
        assertEquals(user.getId(), authenticationDetails.getUserId());
        assertEquals(user.getUsername(), authenticationDetails.getUsername());
        assertEquals(user.getPassword(), authenticationDetails.getPassword());
        assertTrue(authenticationDetails.isEnabled());
        assertTrue(authenticationDetails.isAccountNonExpired());
        assertTrue(authenticationDetails.isAccountNonLocked());
        assertTrue(authenticationDetails.isCredentialsNonExpired());
        assertThat(authenticationDetails.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", authenticationDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void givenNoSuchUserExists_whenUpdateUserProfile_thenExceptionIsThrown() {
        UUID userId = UUID.randomUUID();
        EditProfileRequest editProfileRequest = new EditProfileRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserProfile(userId, editProfileRequest));
    }

    @Test
    void givenSuchUserExists_whenUpdateUserProfile_thenUpdateUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .build();
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .username("Kalin123")
                .firstName("Kalin")
                .lastName("Karolev")
                .profilePicture("www.pic.com")
                .email("kalin@gmail.com")
                .isAnonymousDonator(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserProfile(userId, editProfileRequest);

        assertEquals(editProfileRequest.getUsername(), user.getUsername());
        assertEquals(editProfileRequest.getFirstName(), user.getFirstName());
        assertEquals(editProfileRequest.getLastName(), user.getLastName());
        assertEquals(editProfileRequest.getProfilePicture(), user.getProfilePicture());
        assertEquals(editProfileRequest.getEmail(), user.getEmail());
        assertEquals(editProfileRequest.getIsAnonymousDonator(), user.getIsAnonymousDonator());
    }

    @Test
    void givenUserWithStatusActive_whenSwitchStatus_thenUserBecomesInactive() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .userStatus(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.switchStatus(userId);

        assertEquals(UserStatus.INACTIVE, user.getUserStatus());
    }

    @Test
    void givenUserWithStatusInactive_whenSwitchStatus_thenUserBecomesActive() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .userStatus(UserStatus.INACTIVE)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.switchStatus(userId);

        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
    }

    @Test
    void givenUserWithRoleAdmin_whenSwitchRole_thenUserReceivesUserRole() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .userRole(UserRole.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.switchRole(userId);

        assertEquals(UserRole.USER, user.getUserRole());
    }

    @Test
    void givenUserWithRoleUser_whenSwitchRole_thenUserReceivesAdminRole() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .userRole(UserRole.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.switchRole(userId);

        assertEquals(UserRole.ADMIN, user.getUserRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUsernameIsNotTaken_whenIsUsernameTaken_thenFalse() {
        String username = "Kalin";
        UUID userId = UUID.randomUUID();
        when(userRepository.findByUsernameAndIdNot(username, userId)).thenReturn(new ArrayList<>());

        assertFalse(userService.isUsernameTaken(username, userId));
    }

    @Test
    void givenUsernameIsTaken_whenIsUsernameTaken_thenTrue() {
        String username = "Kalin";
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username(username)
                .build();
        when(userRepository.findByUsernameAndIdNot(username, userId)).thenReturn(List.of(user));

        assertTrue(userService.isUsernameTaken(username, userId));
    }

    @Test
    void givenEmailIsNotTaken_whenIsEmailTaken_thenFalse() {
        String email = "kalin@gmail.com";
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmailAndIdNot(email, userId)).thenReturn(new ArrayList<>());

        assertFalse(userService.isEmailTaken(email, userId));
    }

    @Test
    void givenEmailIsTaken_whenIsUEmailTaken_thenTrue() {
        String email = "kalin@gmail.com";
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .build();
        when(userRepository.findByEmailAndIdNot(email, userId)).thenReturn(List.of(user));

        assertTrue(userService.isEmailTaken(email, userId));
    }

    @Test
    void givenTwoExistingUsers_whenGetAllUsers_thenReturnTwoUsers() {
        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll()).thenReturn(users);

        List<User> allUsers = userService.getAllUsers();
        assertEquals(2, allUsers.size());
    }

    @Test
    void givenUser_whenGetWalletDonationInfo_thenReturnWalletDonationInfo() {
        Wallet wallet = new Wallet();
        User user = User.builder().wallet(wallet).build();

        WalletDonationInfo expectedDonationInfo = WalletDonationInfo.builder()
                .totalDonations("100.00")
                .totalSparks(5)
                .build();

        when(walletService.getWalletDonationInfo(wallet)).thenReturn(expectedDonationInfo);

        WalletDonationInfo returnedDonationInfo = userService.getWalletDonationInfo(user);

        assertEquals(expectedDonationInfo, returnedDonationInfo);
        verify(walletService).getWalletDonationInfo(wallet);
    }
}
