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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record UserResponse(
            String id,
            String firstName,
            String lastName,
            LocalDate dob,
            String email,
            String phoneNumber,
            String addressLine1,
            String addressTown,
            String addressCounty,
            String addressPostcode
    ) {}

    public record CreateUserRequest(
            @NotNull @NotBlank @Size(max = 100) String firstName,
            @NotNull @NotBlank @Size(max = 100) String lastName,
            @NotNull @Past LocalDate dob,
            @NotNull @Email String email,
            @NotNull @NotBlank @Size(min = 8, max = 100) String password,
            @NotNull @NotBlank String phoneNumber,
            @NotNull @NotBlank @Size(max = 100) String addressLine1,
            @NotNull @NotBlank @Size(max = 50) String addressTown,
            @NotNull @NotBlank @Size(max = 50) String addressCounty,
            @NotNull @NotBlank @Pattern(regexp = "^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z]{2}$", 
                    message = "Must be a valid UK postcode") String addressPostcode
    ) {}

    public record UpdateUserRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Past LocalDate dob,
            @Email String email,
            String phoneNumber,
            @Size(max = 100) String addressLine1,
            @Size(max = 50) String addressTown,
            @Size(max = 50) String addressCounty,
            @Pattern(regexp = "^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z]{2}$", 
                    message = "Must be a valid UK postcode") String addressPostcode
    ) {}

    @Operation(summary = "Sign up for Eagle Bank")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        Address address = new Address(
                req.addressLine1(),
                req.addressTown(),
                req.addressCounty(),
                req.addressPostcode()
        );

        User u = userService.createUser(
                new FirstName(req.firstName()),
                new LastName(req.lastName()),
                new DateOfBirth(req.dob()),
                new EmailAddress(req.email()),
                new PhoneNumber(req.phoneNumber()),
                address,
                req.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(u));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch your own user details")
    public ResponseEntity<UserResponse> fetchUser(
            @PathVariable("id") String id,
            Authentication authentication
    ) {
        String callerId = authentication.getName();

        if (!callerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var user = userService.fetchUser(
                UserId.of(UUID.fromString(id)),
                UserId.of(UUID.fromString(callerId))
        );

        return ResponseEntity.ok(toResponse(user));
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

        UserId userId = UserId.of(UUID.fromString(id));
        User currentUser = userService.fetchUser(userId, userId);

        Optional<Address> addressUpdate = getOptionalAddress(req, currentUser);

        User updatedUser = userService.updateUser(
            userId,
            Optional.ofNullable(req.firstName()).map(FirstName::new),
            Optional.ofNullable(req.lastName()).map(LastName::new),
            Optional.ofNullable(req.dob()).map(DateOfBirth::new),
            Optional.ofNullable(req.email()).map(EmailAddress::new),
            Optional.ofNullable(req.phoneNumber()).map(PhoneNumber::new),
            addressUpdate
    );

        return ResponseEntity.ok(toResponse(updatedUser));
    }

    private static Optional<Address> getOptionalAddress(UpdateUserRequest req, User currentUser) {
        Optional<Address> addressUpdate = Optional.empty();
        if (req.addressLine1() != null || req.addressTown() != null ||
                req.addressCounty() != null || req.addressPostcode() != null) {
            addressUpdate = Optional.of(new Address(
                req.addressLine1() != null ? req.addressLine1() : currentUser.getAddress().line1(),
                req.addressTown() != null ? req.addressTown() : currentUser.getAddress().town(),
                req.addressCounty() != null ? req.addressCounty() : currentUser.getAddress().county(),
                req.addressPostcode() != null ? req.addressPostcode() : currentUser.getAddress().postcode()
            ));
        }
        return addressUpdate;
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

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId().value().toString(),
                u.getFirstName().getValue(),
                u.getLastName().getValue(),
                u.getDob().getValue(),
                u.getEmail().value(),
                u.getPhoneNumber().value(),
                u.getAddress().line1(),
                u.getAddress().town(),
                u.getAddress().county(),
                u.getAddress().postcode()
        );
    }
}