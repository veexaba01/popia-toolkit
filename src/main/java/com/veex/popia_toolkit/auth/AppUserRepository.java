package com.veex.popia_toolkit.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>
{
    Optional<Object> findByEmailIgnoreCase(String mail);
}
