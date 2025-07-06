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

    private User createValidUser() {
        return User.create(
                new FirstName("Bob"),
                new LastName("Jones"),
                new DateOfBirth(LocalDate.now().minusYears(25)),
                new EmailAddress("bob@example.com"),
                new PhoneNumber("+447911123456"),
                new Address(
                    "123 High Street",
                    "London",
                    "Greater London",
                    "SW1A 1AA"
                ),
                new PasswordHash("HASH")
        );
    }

    @Test
    void shouldSaveAndFindUserById() {
        User u = createValidUser();
        User saved = repo.save(u);
        
        assertThat(saved.getEmail().value()).isEqualTo("bob@example.com");
        assertThat(saved.getPhoneNumber().value()).isEqualTo("+447911123456");
        assertThat(saved.getAddress().line1()).isEqualTo("123 High Street");
        assertThat(saved.getAddress().town()).isEqualTo("London");
        assertThat(saved.getAddress().county()).isEqualTo("Greater London");
        assertThat(saved.getAddress().postcode()).isEqualTo("SW1A 1AA");

        Optional<User> fetched = repo.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getEmail().value()).isEqualTo("bob@example.com");
        assertThat(fetched.get().getPhoneNumber().value()).isEqualTo("+447911123456");
        assertThat(fetched.get().getAddress().line1()).isEqualTo("123 High Street");
        assertThat(fetched.get().getAddress().town()).isEqualTo("London");
        assertThat(fetched.get().getAddress().county()).isEqualTo("Greater London");
        assertThat(fetched.get().getAddress().postcode()).isEqualTo("SW1A 1AA");
    }

    @Test
    void shouldFindByEmail() {
        User u = createValidUser();
        repo.save(u);

        Optional<User> fetched = repo.findByEmail(new EmailAddress("bob@example.com"));
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getEmail().value()).isEqualTo("bob@example.com");
        assertThat(fetched.get().getPhoneNumber().value()).isEqualTo("+447911123456");
        assertThat(fetched.get().getAddress().line1()).isEqualTo("123 High Street");
    }

    @Test
    void shouldDeleteUser() {
        User u = createValidUser();
        User saved = repo.save(u);
        
        repo.delete(saved);
        
        Optional<User> fetched = repo.findById(saved.getId());
        assertThat(fetched).isEmpty();
    }

    @Test
    void shouldUpdateExistingUser() {
        User original = createValidUser();
        User saved = repo.save(original);

        User updated = saved
                .withEmail(new EmailAddress("bob.new@example.com"))
                .withPhoneNumber(new PhoneNumber("+447911999999"))
                .withAddress(new Address(
                    "456 New Street",
                    "Manchester",
                    "Greater Manchester",
                    "M1 1AA"
                ));
        
        User savedUpdate = repo.save(updated);
        
        Optional<User> fetched = repo.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getEmail().value()).isEqualTo("bob.new@example.com");
        assertThat(fetched.get().getPhoneNumber().value()).isEqualTo("+447911999999");
        assertThat(fetched.get().getAddress().line1()).isEqualTo("456 New Street");
        assertThat(fetched.get().getAddress().town()).isEqualTo("Manchester");
        assertThat(fetched.get().getAddress().county()).isEqualTo("Greater Manchester");
        assertThat(fetched.get().getAddress().postcode()).isEqualTo("M1 1AA");
    }
}