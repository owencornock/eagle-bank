package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.user.*;
import com.eaglebank.eaglebanklogic.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Sign up for Eagle Bank")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest req
    ) {
        User u = userService.createUser(
                new FirstName(req.firstName()),
                new LastName(req.lastName()),
                new DateOfBirth(req.dob()),
                new EmailAddress(req.email()),
                req.password()
        );

        UserResponse resp = new UserResponse(
                u.getId().value().toString(),
                u.getFirstName().getValue(),
                u.getLastName().getValue(),
                u.getDob().getValue(),
                u.getEmail().value()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resp);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch your own user details")
    public ResponseEntity<UserResponse> fetchUser(
            @PathVariable("id") String id,
            Authentication authentication
    ) {
        String callerId = authentication.getName();
        System.out.printf("FetchUser: path-id=%s, jwt-subject=%s%n", id, callerId);

        if (!callerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var domainUser = userService.fetchUser(
                UserId.of(UUID.fromString(id)),
                UserId.of(UUID.fromString(callerId))
        );

        var resp = new UserResponse(
                domainUser.getId().value().toString(),
                domainUser.getFirstName().getValue(),
                domainUser.getLastName().getValue(),
                domainUser.getDob().getValue(),
                domainUser.getEmail().value()
        );
        return ResponseEntity.ok(resp);
    }

    public record UserResponse(
            String id,
            String firstName,
            String lastName,
            LocalDate dob,
            String email
    ) {}

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update your user details")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateUserRequest req,
            Authentication authentication
    ) {
        String callerId = authentication.getName();

        if (!callerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User updatedUser = userService.updateUser(
                UserId.of(UUID.fromString(id)),
                Optional.ofNullable(req.firstName()).map(FirstName::new),
                Optional.ofNullable(req.lastName()).map(LastName::new),
                Optional.ofNullable(req.dob()).map(DateOfBirth::new),
                Optional.ofNullable(req.email()).map(EmailAddress::new)
        );

        var resp = new UserResponse(
                updatedUser.getId().value().toString(),
                updatedUser.getFirstName().getValue(),
                updatedUser.getLastName().getValue(),
                updatedUser.getDob().getValue(),
                updatedUser.getEmail().value()
        );
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your user account")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id") String id,
            Authentication authentication
    ) {
        String callerId = authentication.getName();

        if (!callerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(UserId.of(UUID.fromString(id)));
        return ResponseEntity.noContent().build();

    }

    public record CreateUserRequest(
            @NotNull @NotBlank @Size(max = 100) String firstName,
            @NotNull @NotBlank @Size(max = 100) String lastName,
            @NotNull @Past LocalDate dob,
            @NotNull @Email String email,
            @NotNull @NotBlank @Size(min = 8, max = 100) String password
    ) {
    }

    public record UpdateUserRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Past LocalDate dob,
            @Email String email
    ) {}
}
