package com.weather.api;

import com.weather.model.User;
import com.weather.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST Controller for User management APIs. Provides
 * endpoints for user
 * activation/deactivation.
 */
@RestController
@RequestMapping("/v1/api/user")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User management operations")
public class UserAPI {

    private final UserService userService;

    /**
     * Activates a user account by their username.
     *
     * @param username The username of the user to activate.
     * @return A {@link Mono} emitting the updated {@link User} after activation.
     */
    @Operation(summary = "Activate a user", description = "Activates a user account by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content) })
    @PutMapping("/users/{username}/activate")
    public Mono<User> activateUser(@PathVariable String username) {
        return userService.activateUser(username);
    }

    /**
     * Deactivates a user account by their username.
     *
     * @param username The username of the user to deactivate.
     * @return A {@link Mono} emitting the updated {@link User} after deactivation.
     */
    @Operation(summary = "Deactivate a user", description = "Deactivates a user account by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content) })
    @PutMapping("/users/{username}/deactivate")
    public Mono<User> deactivateUser(@PathVariable String username) {
        return userService.deactivateUser(username);
    }
}
