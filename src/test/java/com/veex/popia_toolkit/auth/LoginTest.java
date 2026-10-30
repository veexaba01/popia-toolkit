package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.audit.AuditAction;
import com.veex.popia_toolkit.audit.AuditService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
public class LoginTest
{
    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuditService auditService;

    private MockMvc mockMvc;
    private UUID organisationId;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        AppUser registered = registrationService.register(
                new RegistrationRequest("Test Clinic", "owner@example.com", PASSWORD));
        organisationId = registered.getOrganisation().getId();
    }

    private boolean hasEvent(AuditAction action)
    {
        return auditService.recent(organisationId).stream().anyMatch(event -> event.getAction() == action);
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

    @Test void loggedInUserSeesTheHomePage() throws Exception
    {
        mockMvc.perform(get("/").with(user("owner@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test void successfulLoginIsAudited() throws Exception
    {
        mockMvc.perform(formLogin().user("owner@example.com").password(PASSWORD));

        assertTrue(hasEvent(AuditAction.LOGIN_SUCCESS), "A successful login should be recorded");
    }

    @Test void failedLoginForAnExistingAccountIsAudited() throws Exception
    {
        mockMvc.perform(formLogin().user("owner@example.com").password("wrong-password-123"));

        assertTrue(hasEvent(AuditAction.LOGIN_FAILURE), "A failed login should be recorded");
        assertFalse(hasEvent(AuditAction.LOGIN_SUCCESS), "A failed login must not look like a success");
    }

    @Test void failedLoginForAnUnknownEmailIsNotRecordedAgainstAnyOrganisation() throws Exception
    {
        mockMvc.perform(formLogin().user("nobody@example.com").password(PASSWORD));

        assertFalse(hasEvent(AuditAction.LOGIN_FAILURE), "There is no organisation to attribute it to");
    }
}