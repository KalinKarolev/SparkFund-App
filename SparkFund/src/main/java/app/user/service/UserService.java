package app.user.service;

import app.exceptions.DomainException;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.WalletDonationInfo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    public UserService(UserRepository _userRepository, PasswordEncoder _passwordEncoder, WalletService _walletService) {
        userRepository = _userRepository;
        passwordEncoder = _passwordEncoder;
        walletService = _walletService;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
        if (userOptional.isPresent()) {
            throw new DomainException("Username [%s] already exist."
                    .formatted(registerRequest.getUsername()));
        }
        userOptional = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent()) {
            throw new DomainException("User with email [%s] already exist."
                    .formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(createUser(registerRequest));
        walletService.createWallet(user);
        log.info("Successfully created a new user with Username [%s] and ID [%s]."
                .formatted(user.getUsername()
                        , user.getId()));

        return user;
    }

    private User createUser(RegisterRequest _registerRequest) {
        return User.builder()
                .username(_registerRequest.getUsername())
                .password(passwordEncoder.encode(_registerRequest.getPassword()))
                .email(_registerRequest.getEmail())
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("No User with this username"));
        return new AuthenticationDetails(user.getId()
                , username
                , user.getPassword()
                , user.getUserRole()
                , user.getUserStatus());
    }

    public void updateUserProfile(UUID id, EditProfileRequest editProfileRequest) {
        User user = getUserById(id);
        user.setUsername(editProfileRequest.getUsername());
        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setProfilePicture(editProfileRequest.getProfilePicture());
        user.setEmail(editProfileRequest.getEmail());
        user.setIsAnonymousDonator(editProfileRequest.getIsAnonymousDonator());
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }

    public WalletDonationInfo getWalletDonationInfo(User user) {
        return walletService.getWalletDonationInfo(user.getWallet());
    }

    public boolean isUsernameTaken(String username, UUID currentUserId) {
        List<User> result = userRepository.findByUsernameAndIdNot(username, currentUserId);
        return !result.isEmpty();
    }

    public boolean isEmailTaken(String username, UUID currentUserId) {
        List<User> result = userRepository.findByEmailAndIdNot(username, currentUserId);
        return !result.isEmpty();
    }

    public User getUserById(UUID _id) {
        return userRepository.findById(_id)
                .orElseThrow(() -> new DomainException("No user with id [%s] found".formatted(_id)));
    }

    public User getAuthenticatedUser(AuthenticationDetails authenticationDetails) {
        return getUserById(UUID.fromString(authenticationDetails.getUserId().toString()));
    }
}
