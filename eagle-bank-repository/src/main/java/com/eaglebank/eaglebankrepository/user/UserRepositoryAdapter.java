package com.eaglebank.eaglebankrepository.user;

import com.eaglebank.eaglebankdomain.user.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository jpa;

    public UserRepositoryAdapter(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return jpa.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public void delete(User user) {
        jpa.deleteById(user.getId().value());
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId().value())
                .firstName(u.getFirstName().getValue())
                .lastName(u.getLastName().getValue())
                .dateOfBirth(u.getDob().getValue())
                .email(u.getEmail().value())
                .passwordHash(u.getPasswordHash().value())
                .build();
    }

    private User toDomain(UserEntity e) {
        return User.rehydrate(
                UserId.of(e.getId()),
                new FirstName(e.getFirstName()),
                new LastName(e.getLastName()),
                new DateOfBirth(e.getDateOfBirth()),
                new EmailAddress(e.getEmail()),
                new PasswordHash(e.getPasswordHash())
        );
    }
}
