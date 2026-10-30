package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import com.veex.popia_toolkit.auth.Role;
import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class AuditPageTest
{
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuditService auditService;

    @Autowired
    private OrganisationRepository organisations;

    private MockMvc mockMvc;
    private Organisation clinicA;
    private Organisation clinicB;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        clinicA = organisations.save(new Organisation("Clinic A"));
        clinicB = organisations.save(new Organisation("Clinic B"));
        auditService.record(clinicA.getId(), "a@example.com", AuditAction.INVENTORY_CREATED, "Item: Alpha records");
        auditService.record(clinicB.getId(), "b@example.com", AuditAction.INVENTORY_CREATED, "Item: Beta records");
    }

    private AppUserPrincipal principalOf(Organisation organisation, Role role)
    {
        return new AppUserPrincipal(UUID.randomUUID(), organisation.getId(), "user@example.com",
                "hash", role, true);
    }

    @Test void anonymousVisitorIsSentToLogin() throws Exception
    {
        mockMvc.perform(get("/admin/audit")).andExpect(status().is3xxRedirection());
    }

    @Test void staffCannotSeeTheAuditLog() throws Exception
    {
        mockMvc.perform(get("/admin/audit").with(user(principalOf(clinicA, Role.STAFF))))
                .andExpect(status().isForbidden());
    }

    @Test void adminSeesOnlyTheirOwnOrganisationsEvents() throws Exception
    {
        // Act
        String page = mockMvc.perform(get("/admin/audit").with(user(principalOf(clinicA, Role.ADMIN))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Assert
        assertTrue(page.contains("Item: Alpha records"), "Clinic A's event should be shown");
        assertFalse(page.contains("Item: Beta records"), "Clinic B's event must never appear for clinic A");
    }
}