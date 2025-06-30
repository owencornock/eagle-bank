package com.eaglebank.eaglebanklogic.user;

import com.eaglebank.eaglebankdomain.account.Account;
import com.eaglebank.eaglebankdomain.account.AccountRepository;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.exception.UserHasAccountsException;
import com.eaglebank.eaglebankdomain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder encoder;
    @Mock
    private AccountRepository accountRepository;

    private UserService service;

    private FirstName fn;
    private LastName ln;
    private DateOfBirth dob;
    private EmailAddress email;
    private String rawPassword;
    private String hashedPassword;
    private User existingUser;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new UserService(userRepository, accountRepository, encoder);

        fn           = new FirstName("Alice");
        ln           = new LastName("Smith");
        dob          = new DateOfBirth(LocalDate.now().minusYears(30));
        email        = new EmailAddress("alice@example.com");
        rawPassword  = "secret123";
        hashedPassword = "HASHED";

        existingUser = User.create(fn, ln, dob, email, new PasswordHash(hashedPassword));}

    @Test
    void shouldCreateAndSaveUserWithUniqueEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(encoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.createUser(fn, ln, dob, email, rawPassword);

        assertNotNull(result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword, result.getPasswordHash().value());

        verify(userRepository).findByEmail(email);
        verify(encoder).encode(rawPassword);
        verify(userRepository).save(result);
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenUserIsCreatedWithDuplicateEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(InvalidUserDataException.class, () ->
                service.createUser(fn, ln, dob, email, rawPassword)
        );
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGetUserWhenIdIsProvided() {
        UserId id = existingUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        User result = service.fetchUser(id, id);
        assertSame(existingUser, result);
        verify(userRepository).findById(id);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserIdIsDifferent() {
        UserId id     = existingUser.getId();
        UserId other  = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.fetchUser(id, other)
        );
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.fetchUser(id, id)
        );
        verify(userRepository).findById(id);
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange
        EmailAddress email = new EmailAddress("test@example.com");
        User expectedUser = User.create(
                new FirstName("John"),
                new LastName("Doe"),
                new DateOfBirth(LocalDate.now().minusYears(25)),
                email,
                new PasswordHash("hashedPassword")
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = service.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldReturnEmptyOptionalWhenEmailNotFound() {
        EmailAddress email = new EmailAddress("nonexistent@example.com");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = service.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldPropagateExceptionWhenEmailIsNull() {
        when(userRepository.findByEmail(null))
                .thenThrow(new NullPointerException("Email cannot be null"));

        assertThrows(NullPointerException.class,
                () -> service.findByEmail(null));
        verify(userRepository).findByEmail(null);
    }

    @Test
    void shouldUpdateUserEmailWhenNewEmailIsUnique() {
        UserId id = existingUser.getId();
        EmailAddress newEmail = new EmailAddress("new@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateUser(
                id,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newEmail)
        );

        assertEquals(newEmail, result.getEmail());
        assertEquals(existingUser.getFirstName(), result.getFirstName());
        assertEquals(existingUser.getLastName(), result.getLastName());
        assertEquals(existingUser.getDob(), result.getDob());
        verify(userRepository).findById(id);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateAllUserFieldsWhenProvided() {
        UserId id = existingUser.getId();
        FirstName newFirstName = new FirstName("Bob");
        LastName newLastName = new LastName("Johnson");
        DateOfBirth newDob = new DateOfBirth(LocalDate.now().minusYears(25));
        EmailAddress newEmail = new EmailAddress("new@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateUser(
                id,
                Optional.of(newFirstName),
                Optional.of(newLastName),
                Optional.of(newDob),
                Optional.of(newEmail)
        );

        assertEquals(newFirstName, result.getFirstName());
        assertEquals(newLastName, result.getLastName());
        assertEquals(newDob, result.getDob());
        assertEquals(newEmail, result.getEmail());
        verify(userRepository).findById(id);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenUpdatingToExistingEmail() {
        UserId id = existingUser.getId();
        EmailAddress newEmail = new EmailAddress("existing@example.com");
        User otherUser = User.create(
                new FirstName("Other"),
                new LastName("User"),
                new DateOfBirth(LocalDate.now().minusYears(30)),
                newEmail,
                new PasswordHash("hash")
        );

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(otherUser));

        assertThrows(InvalidUserDataException.class, () ->
                service.updateUser(
                        id,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(newEmail)
                )
        );

        verify(userRepository).findById(id);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentUser() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.updateUser(
                        id,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        verify(userRepository).findById(id);
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserWhenUserExistsAndHasNoAccounts() {
        UserId id = existingUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(accountRepository.findByOwner(id)).thenReturn(List.of());

        service.deleteUser(id);

        verify(userRepository).findById(id);
        verify(accountRepository).findByOwner(id);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void shouldThrowUserHasAccountsExceptionWhenDeletingUserWithAccounts() {
        UserId id = existingUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(accountRepository.findByOwner(id)).thenReturn(List.of(mock(Account.class)));

        assertThrows(UserHasAccountsException.class, () ->
                service.deleteUser(id)
        );

        verify(userRepository).findById(id);
        verify(accountRepository).findByOwner(id);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentUser() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.deleteUser(id)
        );

        verify(userRepository).findById(id);
        verify(accountRepository, never()).findByOwner(any());
        verify(userRepository, never()).delete(any());
    }
}