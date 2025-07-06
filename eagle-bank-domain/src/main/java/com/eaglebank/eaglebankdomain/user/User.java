package com.eaglebank.eaglebankdomain.user;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class User {
    private final UserId       id;
    private final FirstName    firstName;
    private final LastName     lastName;
    private final DateOfBirth  dob;
    private final EmailAddress email;
    private final PhoneNumber  phoneNumber;
    private final Address      address;
    private final PasswordHash passwordHash;

    private User(UserId id,
                 FirstName firstName,
                 LastName lastName,
                 DateOfBirth dob,
                 EmailAddress email,
                 PhoneNumber phoneNumber,
                 Address address,
                 PasswordHash passwordHash) {
        this.id = Objects.requireNonNull(id);
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.dob = Objects.requireNonNull(dob);
        this.email = Objects.requireNonNull(email);
        this.phoneNumber = Objects.requireNonNull(phoneNumber);
        this.address = Objects.requireNonNull(address);
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public static User rehydrate(UserId id,
                                 FirstName fn,
                                 LastName ln,
                                 DateOfBirth dob,
                                 EmailAddress email,
                                 PhoneNumber pn,
                                 Address addr,
                                 PasswordHash hash) {
        return new User(id, fn, ln, dob, email, pn, addr, hash);
    }

    public static User create(FirstName firstName,
                              LastName lastName,
                              DateOfBirth dob,
                              EmailAddress email,
                              PhoneNumber phoneNumber,
                              Address address,
                              PasswordHash passwordHash) {
        return new User(UserId.newId(), firstName, lastName, dob, email, phoneNumber, address, passwordHash);
    }

    public User withEmail(EmailAddress newEmail) {
        return new User(this.id, this.firstName, this.lastName, this.dob, newEmail, this.phoneNumber, this.address, this.passwordHash);
    }

    public User withFirstName(FirstName newFirstName) {
        return new User(this.id, newFirstName, this.lastName, this.dob, this.email, this.phoneNumber, this.address, this.passwordHash);
    }

    public User withLastName(LastName newLastName) {
        return new User(this.id, this.firstName, newLastName, this.dob, this.email, this.phoneNumber, this.address, this.passwordHash);
    }

    public User withDob(DateOfBirth newDob) {
        return new User(this.id, this.firstName, this.lastName, newDob, this.email, this.phoneNumber, this.address, this.passwordHash);
    }

    public User withPhoneNumber(PhoneNumber newPn) {
        return new User(this.id, this.firstName, this.lastName, this.dob, this.email, newPn, this.address, this.passwordHash);
    }

    public User withAddress(Address newAddr) {
        return new User(this.id, this.firstName, this.lastName, this.dob, this.email, this.phoneNumber, newAddr, this.passwordHash);
    }


}
