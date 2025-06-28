package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.exception.UserHasAccountsException;
import com.eaglebank.eaglebankdomain.exception.UserNotFoundException;
import com.eaglebank.eaglebankdomain.user.*;
import com.eaglebank.eaglebanklogic.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String BASE = "/v1/users";
    private static final UUID EXISTING_ID = UUID.randomUUID();

    @Mock
    private UserService userService;
    private static final String EXISTING_ID_STR = EXISTING_ID.toString();
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userController, "userService", userService);
    }


    private UsernamePasswordAuthenticationToken auth(String id) {
        return new UsernamePasswordAuthenticationToken(id, null);
    }

    @Test
    @DisplayName("POST /v1/users - success")
    void createUserSuccess() throws Exception {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        User u = User.rehydrate(
                UserId.of(EXISTING_ID), new FirstName("Alice"), new LastName("Smith"),
                new DateOfBirth(dob), new EmailAddress("alice@example.com"), new PasswordHash("h"));
        given(userService.createUser(any(), any(), any(), any(), anyString())).willReturn(u);

        String payload = "{"
                + "\"firstName\":\"Alice\","
                + "\"lastName\":\"Smith\","
                + "\"dob\":\"1990-01-01\","
                + "\"email\":\"alice@example.com\","
                + "\"password\":\"secret123\""
                + "}";

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(u.getId().value().toString()))
                .andExpect(jsonPath("$.firstName").value(u.getFirstName().getValue()));
    }

    @Test
    @DisplayName("POST /v1/users - validation fail")
    void createUserValidationFail() throws Exception {
        String payload = "{ \"firstName\": \"\" }";
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/users/{id} - authorized")
    void fetchUserSuccess() throws Exception {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        User u = User.rehydrate(
                UserId.of(EXISTING_ID), new FirstName("Bob"), new LastName("Lee"),
                new DateOfBirth(dob), new EmailAddress("bob@example.com"), new PasswordHash("h"));
        given(userService.fetchUser(eq(UserId.of(EXISTING_ID)), eq(UserId.of(EXISTING_ID)))).willReturn(u);

        mvc.perform(get(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    @DisplayName("GET /v1/users/{id} - forbidden")
    void fetchUserForbidden() throws Exception {
        mvc.perform(get(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(UUID.randomUUID().toString()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /v1/users/{id} - not found")
    void fetchUserNotFound() throws Exception {
        given(userService.fetchUser(any(), any())).willThrow(new ResourceNotFoundException("User not found"));
        mvc.perform(get(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /v1/users/{id} - success")
    void updateUserSuccess() throws Exception {
        LocalDate dob = LocalDate.of(1985, 5, 5);
        User u = User.rehydrate(
                UserId.of(EXISTING_ID), new FirstName("Al"), new LastName("Go"),
                new DateOfBirth(dob), new EmailAddress("al@example.com"), new PasswordHash("h"));
        given(userService.updateUser(
                eq(UserId.of(EXISTING_ID)), any(), any(), any(), any()
        )).willReturn(u);

        String payload = "{ \"firstName\": \"Al\" }";
        mvc.perform(patch(BASE + "/" + EXISTING_ID_STR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Al"));
    }

    @Test
    @DisplayName("PATCH /v1/users/{id} - forbidden")
    void updateUserForbidden() throws Exception {
        mvc.perform(patch(BASE + "/" + EXISTING_ID_STR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ }")
                        .principal(auth(UUID.randomUUID().toString()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /v1/users/{id} - not found")
    void updateUserNotFound() throws Exception {
        given(userService.updateUser(any(), any(), any(), any(), any()))
                .willThrow(new ResourceNotFoundException("User not found"));
        mvc.perform(patch(BASE + "/" + EXISTING_ID_STR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"X\"}")
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /v1/users/{id} - success")
    void deleteUserSuccess() throws Exception {
        mvc.perform(delete(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/users/{id} - forbidden")
    void deleteUserForbidden() throws Exception {
        mvc.perform(delete(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(UUID.randomUUID().toString()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /v1/users/{id} - conflict")
    void deleteUserConflict() throws Exception {
        doThrow(new UserHasAccountsException("Has accounts")).when(userService).deleteUser(UserId.of(EXISTING_ID));
        mvc.perform(delete(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /v1/users/{id} - not found")
    void deleteUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("No user")).when(userService).deleteUser(UserId.of(EXISTING_ID));
        mvc.perform(delete(BASE + "/" + EXISTING_ID_STR)
                        .principal(auth(EXISTING_ID_STR))
                )
                .andExpect(status().isNotFound());
    }
}