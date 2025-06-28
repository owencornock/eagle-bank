package com.eaglebank.eaglebanklogic.user;

import com.eaglebank.eaglebankdomain.account.AccountRepository;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.exception.UserHasAccountsException;
import com.eaglebank.eaglebankdomain.user.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final AccountRepository accountRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, AccountRepository accountRepo, PasswordEncoder encoder) {
        this.repo = repo;
        this.accountRepo = accountRepo;
        this.encoder = encoder;
    }

    public User createUser(FirstName fn,
                           LastName ln,
                           DateOfBirth dob,
                           EmailAddress email,
                           String rawPassword) {
        if (repo.findByEmail(email).isPresent()) {
            throw new InvalidUserDataException("Email already in use");
        }
        User u = User.create(fn, ln, dob, email, new PasswordHash(encoder.encode(rawPassword)));
        return repo.save(u);
    }

    public User fetchUser(UserId id, UserId callerId) {
        if (!id.equals(callerId)) {
            throw new ForbiddenException("Cannot fetch another user’s data");
        }
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(UserId id,
                           Optional<FirstName> firstName,
                           Optional<LastName> lastName,
                           Optional<DateOfBirth> dob,
                           Optional<EmailAddress> email) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (email.isPresent()) {
            if (repo.findByEmail(email.get()).isPresent()) {
                throw new InvalidUserDataException("Email already in use");
            }
            user = user.withEmail(email.get());
        }
        if (firstName.isPresent()) {
            user = user.withFirstName(firstName.get());
        }
        if (lastName.isPresent()) {
            user = user.withLastName(lastName.get());
        }
        if (dob.isPresent()) {
            user = user.withDob(dob.get());
        }

        return repo.save(user);
    }

    @Transactional
    public void deleteUser(UserId id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!accountRepo.findByOwner(id).isEmpty()) {
            throw new UserHasAccountsException("User has associated bank accounts");
        }

        repo.delete(user);
    }

    public Optional<User> findByEmail(EmailAddress email) {
        return repo.findByEmail(email);
    }
}
