package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class AppUserRepositoryTest
{
    @Autowired
    private OrganisationRepository organisations;

    @Autowired
    private AppUserRepository users;

    @Test void findsUserByEmailIgnoringCase()
    {
        Organisation org = organisations.save(new Organisation("Test Clinic"));
        users.save(new AppUser(org, "owner@example.com", "not-a-real-hash", Role.ADMIN));

        assertTrue(users.findByEmailIgnoreCase("OWNER@example.com").isPresent());
    }

    @Test void rejectsDuplicateEmail()
    {
        Organisation org = organisations.save(new Organisation("Test Clinic"));
        users.save(new AppUser(org, "owner@example.com", "not-a-real-hash", Role.ADMIN));

        assertThrows(DataIntegrityViolationException.class, () ->
                users.saveAndFlush(new AppUser(org, "owner@example.com", "another-hash", Role.STAFF)));
    }
}