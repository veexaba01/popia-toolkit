package com.veex.popia_toolkit.auth;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@Transactional
public class LoginRateLimitTest
{
    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private LoginAttemptTracker tracker;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String registerFreshUser()
    {
        String email = "limit-" + UUID.randomUUID() + "@example.com";
        registrationService.register(new RegistrationRequest("Test Clinic", email, PASSWORD));
        return email;
    }

    private void failToLogIn(String email, int times) throws Exception
    {
        for (int i = 0; i < times; i++)
        {
            mockMvc.perform(formLogin().user(email).password("wrong-password-123"));
        }
    }

    @Test void correctPasswordIsRefusedAfterTooManyFailures() throws Exception
    {
        // Arrange
        String email = registerFreshUser();
        failToLogIn(email, 5);

        // Act and Assert
        mockMvc.perform(formLogin().user(email).password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test void loginStillWorksBelowTheLimit() throws Exception
    {
        // Arrange
        String email = registerFreshUser();
        failToLogIn(email, 4);

        // Act and Assert
        mockMvc.perform(formLogin().user(email).password(PASSWORD))
                .andExpect(authenticated().withUsername(email));
    }

    @Test void aSuccessfulLoginClearsEarlierFailures() throws Exception
    {
        // Arrange
        String email = registerFreshUser();
        failToLogIn(email, 4);
        mockMvc.perform(formLogin().user(email).password(PASSWORD));
        failToLogIn(email, 4);

        // Act and Assert
        mockMvc.perform(formLogin().user(email).password(PASSWORD))
                .andExpect(authenticated().withUsername(email));
    }

    @Test void unknownEmailsAreCountedToo() throws Exception
    {
        // Arrange
        String unknown = "nobody-" + UUID.randomUUID() + "@example.com";

        // Act
        failToLogIn(unknown, 5);

        // Assert
        assertTrue(tracker.isBlocked(unknown), "Counting unknown emails stops lockouts revealing real accounts");
    }
}