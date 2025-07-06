package com.eaglebank.eaglebankrepository.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    private UUID id;

    @Column(name="first_name", nullable=false, length=50)
    private String firstName;

    @Column(name="last_name", nullable=false, length=50)
    private String lastName;

    @Column(name="date_of_birth", nullable=false)
    private LocalDate dateOfBirth;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;
    
    @Column(name="phone_number", nullable=false)
    private String phoneNumber;
    
    @Column(name="address_line1", nullable=false, length=100)
    private String addressLine1;
    
    @Column(name="address_town", nullable=false, length=50)
    private String addressTown;
    
    @Column(name="address_county", nullable=false, length=50)
    private String addressCounty;
    
    @Column(name="address_postcode", nullable=false, length=8)
    private String addressPostcode;
}