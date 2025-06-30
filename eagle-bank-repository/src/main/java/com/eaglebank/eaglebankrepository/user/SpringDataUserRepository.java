package com.eaglebank.eaglebankrepository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository
        extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}