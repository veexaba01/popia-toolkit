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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class PasswordChangeFlowTest
{
    private static final String OLD_PASSWORD = "old-password-1234";
    private static final String NEW_PASSWORD = "new-password-5678";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuditService auditService;

    private MockMvc mockMvc;
    private AppUser registered;
    private String email;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        email = "change-" + UUID.randomUUID() + "@example.com";
        registered = registrationService.register(new RegistrationRequest("Test Clinic", email, OLD_PASSWORD));
    }

    private AppUserPrincipal principal()
    {
        return new AppUserPrincipal(registered.getId(), registered.getOrganisation().getId(), email,
                registered.getPasswordHash(), Role.ADMIN, true);
    }

    @Test void anonymousVisitorIsSentToLogin() throws Exception
    {
        mockMvc.perform(get("/account/password")).andExpect(status().is3xxRedirection());
    }

    @Test void loggedInUserCanOpenThePage() throws Exception
    {
        mockMvc.perform(get("/account/password").with(user(principal()))).andExpect(status().isOk());
    }

    @Test void postWithoutACsrfTokenIsForbidden() throws Exception
    {
        mockMvc.perform(post("/account/password").with(user(principal()))).andExpect(status().isForbidden());
    }

    @Test void afterAChangeOnlyTheNewPasswordLogsIn() throws Exception
    {
        // Act
        mockMvc.perform(post("/account/password").with(user(principal())).with(csrf())
                        .param("currentPassword", OLD_PASSWORD)
                        .param("newPassword", NEW_PASSWORD)
                        .param("confirmPassword", NEW_PASSWORD))
                .andExpect(redirectedUrl("/account/password?changed"));

        // Assert
        mockMvc.perform(formLogin().user(email).password(OLD_PASSWORD)).andExpect(unauthenticated());
        mockMvc.perform(formLogin().user(email).password(NEW_PASSWORD)).andExpect(authenticated());
        assertTrue(auditService.recent(registered.getOrganisation().getId()).stream()
                        .anyMatch(event -> event.getAction() == AuditAction.PASSWORD_CHANGED),
                "The change should appear in the audit log");
    }

    @Test void aWrongCurrentPasswordChangesNothing() throws Exception
    {
        // Act
        mockMvc.perform(post("/account/password").with(user(principal())).with(csrf())
                        .param("currentPassword", "not-the-password-1")
                        .param("newPassword", NEW_PASSWORD)
                        .param("confirmPassword", NEW_PASSWORD))
                .andExpect(status().isOk());

        // Assert
        mockMvc.perform(formLogin().user(email).password(OLD_PASSWORD)).andExpect(authenticated());
    }
}