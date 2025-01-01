package com.weather.service;

import com.weather.dto.UserRegistrationRequest;
import com.weather.exception.InvalidCredentialsException;
import com.weather.exception.UserAlreadyExistsException;
import com.weather.exception.UserNotFoundException;
import com.weather.model.User;
import com.weather.repository.UserRepository;
import com.weather.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private String rawPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        rawPassword = "testPassword";
        encodedPassword = "encodedPassword";

        testUser = User.builder()
                .id("testId")
                .username("testUser")
                .password(encodedPassword)
                .postalCode("12345")
                .active(true)
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("testUser")
                .password(rawPassword)
                .postalCode("12345")
                .build();

    }

    @Test
    void createUserNewUserSuccessTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.createUser(registrationRequest))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals(testUser.getUsername(), user.getUsername());
                    assertEquals(testUser.getPassword(), user.getPassword());
                    assertEquals(testUser.getPostalCode(), user.getPostalCode());
                    assertTrue(user.isActive());
                    assertEquals(Collections.singletonList("ROLE_USER"), user.getRoles());
                })
                .verifyComplete();
    }

    @Test
    void createUserExistingUserThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.createUser(registrationRequest))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }

    @Test
    void authenticateValidCredentialsSuccessTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        StepVerifier.create(userService.authenticate("testUser", rawPassword))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals(testUser.getUsername(), user.getUsername());
                    assertEquals(testUser.getPassword(), user.getPassword());
                })
                .verifyComplete();
    }

    @Test
    void authenticateInvalidCredentialsThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        StepVerifier.create(userService.authenticate("testUser", rawPassword))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void authenticateUserNotFoundThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.authenticate("testUser", rawPassword))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void findByUsernameExistingUserSuccessTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findByUsername("testUser"))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals(testUser.getUsername(), user.getUsername());
                })
                .verifyComplete();
    }

    @Test
    void findByUsernameNonExistingUserThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.findByUsername("testUser"))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void activateUserExistingUserSuccessTest() {
        User inactiveUser = User.builder()
                .username("testUser")
                .active(false)
                .build();

        User activatedUser = User.builder()
                .username("testUser")
                .active(true)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(activatedUser));

        StepVerifier.create(userService.activateUser("testUser"))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertTrue(user.isActive());
                })
                .verifyComplete();
    }

    @Test
    void activateUserNonExistingUserThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.activateUser("testUser"))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void deactivateUserExistingUserSuccessTest() {
        User activeUser = User.builder()
                .username("testUser")
                .active(true)
                .build();

        User deactivatedUser = User.builder()
                .username("testUser")
                .active(false)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(deactivatedUser));

        StepVerifier.create(userService.deactivateUser("testUser"))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertTrue(!user.isActive());
                })
                .verifyComplete();
    }

    @Test
    void deactivateUserNonExistingUserThrowsExceptionTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.deactivateUser("testUser"))
                .expectError(UserNotFoundException.class)
                .verify();
    }
}
