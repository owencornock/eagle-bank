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

    private UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .firstName(user.getFirstName().getValue())
                .lastName(user.getLastName().getValue())
                .dateOfBirth(user.getDob().getValue())
                .email(user.getEmail().value())
                .passwordHash(user.getPasswordHash().value())
                .phoneNumber(user.getPhoneNumber().value())
                .addressLine1(user.getAddress().line1())
                .addressTown(user.getAddress().town())
                .addressCounty(user.getAddress().county())
                .addressPostcode(user.getAddress().postcode())
                .build();
    }

    private User toDomain(UserEntity entity) {
        return User.rehydrate(
                UserId.of(entity.getId()),
                new FirstName(entity.getFirstName()),
                new LastName(entity.getLastName()),
                new DateOfBirth(entity.getDateOfBirth()),
                new EmailAddress(entity.getEmail()),
                new PhoneNumber(entity.getPhoneNumber()),
                new Address(
                        entity.getAddressLine1(),
                        entity.getAddressTown(),
                        entity.getAddressCounty(),
                        entity.getAddressPostcode()
                ),
                new PasswordHash(entity.getPasswordHash())
        );
    }
}