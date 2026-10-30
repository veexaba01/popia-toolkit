package com.veex.popia_toolkit.auth;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordChangeService
{
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public PasswordChangeService(AppUserRepository users, PasswordEncoder passwordEncoder)
    {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword)
    {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
        {
            throw new InvalidCurrentPasswordException();
        }

        user.changePasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);
    }
}