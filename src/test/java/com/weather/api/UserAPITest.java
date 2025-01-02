package com.weather.api;

import com.weather.dto.TokenRequest;
import com.weather.dto.UserRegistrationRequest;
import com.weather.model.User;
import com.weather.security.JwtService;
import com.weather.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAPITest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserAPI userAPI;

    private User testUser;
    private String testToken;
    private UserRegistrationRequest registrationRequest;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testUser")
                .password("password123")
                .active(true)
                .build();

        testToken = "test.jwt.token";

        registrationRequest = UserRegistrationRequest.builder()
                .username("testUser")
                .password("password123")
                .build();

        tokenRequest = TokenRequest.builder()
                .username("testUser")
                .password("password123")
                .build();
    }

    @Test
    void activateUserExistingUserReturnsActivatedUserTest() {
        User activatedUser = User.builder()
                .username(testUser.getUsername())
                .active(true)
                .build();

        when(userService.activateUser(testUser.getUsername()))
                .thenReturn(Mono.just(activatedUser));

        StepVerifier.create(userAPI.activateUser(testUser.getUsername()))
                .assertNext(user -> {
                    assertEquals(testUser.getUsername(), user.getUsername());
                    assertTrue(user.isActive());
                })
                .verifyComplete();
    }

    @Test
    void deactivateUserExistingUserReturnsDeactivatedUserTest() {
        User deactivatedUser = User.builder()
                .username(testUser.getUsername())
                .active(false)
                .build();

        when(userService.deactivateUser(testUser.getUsername()))
                .thenReturn(Mono.just(deactivatedUser));

        StepVerifier.create(userAPI.deactivateUser(testUser.getUsername()))
                .assertNext(user -> {
                    assertEquals(testUser.getUsername(), user.getUsername());
                    assertFalse(user.isActive());
                })
                .verifyComplete();
    }
}
