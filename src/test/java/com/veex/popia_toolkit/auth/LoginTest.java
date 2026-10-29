package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@Transactional
public class LoginTest
{
    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RegistrationService registrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        registrationService.register(new RegistrationRequest("Test Clinic", "owner@example.com", PASSWORD));
    }

    @Test void registeredUserCanLogIn() throws Exception
    {
        mockMvc.perform(formLogin().user("owner@example.com").password(PASSWORD))
                .andExpect(authenticated().withUsername("owner@example.com"));
    }

    @Test void wrongPasswordIsRejected() throws Exception
    {
        mockMvc.perform(formLogin().user("owner@example.com").password("wrong-password-123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test void unknownEmailGivesTheSameResultAsAWrongPassword() throws Exception
    {
        mockMvc.perform(formLogin().user("nobody@example.com").password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }
}