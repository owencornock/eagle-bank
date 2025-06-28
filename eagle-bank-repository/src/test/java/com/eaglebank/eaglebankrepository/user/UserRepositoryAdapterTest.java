package com.eaglebank.eaglebankrepository.user;

import com.eaglebank.eaglebankdomain.user.*;
import com.eaglebank.eaglebankrepository.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepository repo;

    @Test
    void shouldSaveAndFindUserById() {
        User u = User.create(
                new FirstName("Bob"),
                new LastName("Jones"),
                new DateOfBirth(LocalDate.now().minusYears(25)),
                new EmailAddress("bob@example.com"),
                new PasswordHash("HASH")
        );
        User saved = repo.save(u);
        assertThat(saved.getEmail().value()).isEqualTo("bob@example.com");

        Optional<User> fetched = repo.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getEmail().value()).isEqualTo("bob@example.com");
    }
}