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
    @Mock 
    private PasswordEncoder encoder;
    @Mock
    private AccountRepository accountRepository;

    private UserService service;

    private FirstName fn;
    private LastName ln;
    private DateOfBirth dob;
    private EmailAddress email;
    private String rawPassword;
    private String hashedPassword;
    private PhoneNumber phoneNumber;
    private Address address;
    private User existingUser;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new UserService(userRepository, accountRepository, encoder);

        fn = new FirstName("Alice");
        ln = new LastName("Smith");
        dob = new DateOfBirth(LocalDate.now().minusYears(30));
        email = new EmailAddress("alice@example.com");
        rawPassword = "secret123";
        hashedPassword = "HASHED";
        phoneNumber = new PhoneNumber("+447911123456");
        address = new Address(
            "123 High Street",
            "London",
            "Greater London",
            "SW1A 1AA"
        );

        existingUser = User.create(fn, ln, dob, email, phoneNumber, address, 
            new PasswordHash(hashedPassword));
    }

    @Test
    void shouldCreateAndSaveUserWithUniqueEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(encoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.createUser(fn, ln, dob, email, phoneNumber, address, rawPassword);

        assertNotNull(result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword, result.getPasswordHash().value());
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals(address, result.getAddress());

        verify(userRepository).findByEmail(email);
        verify(encoder).encode(rawPassword);
        verify(userRepository).save(result);
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenUserIsCreatedWithDuplicateEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(InvalidUserDataException.class, () ->
                service.createUser(fn, ln, dob, email, phoneNumber, address, rawPassword)
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
        UserId id = existingUser.getId();
        UserId other = UserId.newId();

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
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        Optional<User> result = service.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(existingUser, result.get());
        assertEquals(phoneNumber, result.get().getPhoneNumber());
        assertEquals(address, result.get().getAddress());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldReturnEmptyOptionalWhenEmailNotFound() {
        EmailAddress nonExistentEmail = new EmailAddress("nonexistent@example.com");
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        Optional<User> result = service.findByEmail(nonExistentEmail);

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void shouldUpdateUserPhoneNumber() {
        UserId id = existingUser.getId();
        PhoneNumber newPhone = new PhoneNumber("+447911999999");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateUser(
                id,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newPhone),
                Optional.empty()
        );

        assertEquals(newPhone, result.getPhoneNumber());
        assertEquals(existingUser.getAddress(), result.getAddress());
        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserAddress() {
        UserId id = existingUser.getId();
        Address newAddress = new Address(
            "456 New Street",
            "Manchester",
            "Greater Manchester",
            "M1 1AA"
        );

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateUser(
                id,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newAddress)
        );

        assertEquals(newAddress, result.getAddress());
        assertEquals(existingUser.getPhoneNumber(), result.getPhoneNumber());
        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
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