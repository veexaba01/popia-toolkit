package com.veex.popia_toolkit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PasswordEncoderTest
{
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test void hashDiffersFromRawPasswordAndMatchesIt()
    {
        String raw = "correct-horse-battery-staple";

        String hash = passwordEncoder.encode(raw);

        assertNotEquals(raw, hash);
        assertTrue(passwordEncoder.matches(raw, hash));
        assertFalse(passwordEncoder.matches("wrong-password", hash));
    }

    @Test void samePasswordProducesDifferentHashesBecauseOfSalting()
    {
        String raw = "correct-horse-battery-staple";

        assertNotEquals(passwordEncoder.encode(raw), passwordEncoder.encode(raw));
    }
}