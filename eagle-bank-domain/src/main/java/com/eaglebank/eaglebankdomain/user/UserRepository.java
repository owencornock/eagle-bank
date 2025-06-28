package com.eaglebank.eaglebankdomain.user;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(EmailAddress email);
    void delete(User user);
}
