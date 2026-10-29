package com.veex.popia_toolkit.inventory;

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
public class InventoryPageTest
{
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InventoryService service;

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
        service.create(clinicA.getId(), new InventoryItemDetails("Patient contacts", "Booking", "Software", false));
        service.create(clinicB.getId(), new InventoryItemDetails("Learner records", "Admissions", "Cabinet", false));
    }

    private AppUserPrincipal staffOf(Organisation organisation)
    {
        return new AppUserPrincipal(UUID.randomUUID(), organisation.getId(), "staff@example.com",
                "hash", Role.STAFF, true);
    }

    @Test void anonymousVisitorIsSentToLogin() throws Exception
    {
        mockMvc.perform(get("/inventory")).andExpect(status().is3xxRedirection());
    }

    @Test void userSeesOnlyTheItemsOfTheirOwnOrganisation() throws Exception
    {
        // Act
        String page = mockMvc.perform(get("/inventory").with(user(staffOf(clinicA))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Assert
        assertTrue(page.contains("Patient contacts"), "Clinic A's item should be shown");
        assertFalse(page.contains("Learner records"), "Clinic B's item must never appear for clinic A");
    }

        @Test void userCanOpenTheAddForm() throws Exception
    {
        mockMvc.perform(get("/inventory/new").with(user(staffOf(clinicA))))
                .andExpect(status().isOk());
    }
}