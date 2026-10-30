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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
    private InventoryItem itemA;
    private InventoryItem itemB;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        clinicA = organisations.save(new Organisation("Clinic A"));
        clinicB = organisations.save(new Organisation("Clinic B"));
        itemA = service.create(clinicA.getId(), new InventoryItemDetails("Patient contacts", "Booking", "Software", false));
        itemB = service.create(clinicB.getId(), new InventoryItemDetails("Learner records", "Admissions", "Cabinet", false));
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

    @Test void ownerCanOpenTheEditPage() throws Exception
    {
        mockMvc.perform(get("/inventory/" + itemA.getId() + "/edit").with(user(staffOf(clinicA))))
                .andExpect(status().isOk());
    }

        @Test void unknownItemGivesNotFound() throws Exception
    {
        mockMvc.perform(get("/inventory/" + UUID.randomUUID() + "/edit").with(user(staffOf(clinicA))))
                .andExpect(status().isNotFound());
    }

    @Test void anotherOrganisationsItemGivesNotFound() throws Exception
    {
        mockMvc.perform(get("/inventory/" + itemB.getId() + "/edit").with(user(staffOf(clinicA))))
                .andExpect(status().isNotFound());
    }

    @Test void ownerCanUpdateTheirItem() throws Exception
    {
        mockMvc.perform(post("/inventory/" + itemA.getId()).with(user(staffOf(clinicA))).with(csrf())
                        .param("name", "Patient records")
                        .param("purpose", "Treatment")
                        .param("storageLocation", "Locked cabinet"))
                .andExpect(status().is3xxRedirection());

        assertEquals("Patient records", service.get(clinicA.getId(), itemA.getId()).getName());
    }

    @Test void cannotUpdateAnotherOrganisationsItem() throws Exception
    {
        mockMvc.perform(post("/inventory/" + itemB.getId()).with(user(staffOf(clinicA))).with(csrf())
                        .param("name", "Hacked")
                        .param("purpose", "Hacked")
                        .param("storageLocation", "Hacked"))
                .andExpect(status().isNotFound());

        assertEquals("Learner records", service.get(clinicB.getId(), itemB.getId()).getName(),
                "The other organisation's item must be unchanged");
    }

    private AppUserPrincipal adminOf(Organisation organisation)
    {
        return new AppUserPrincipal(UUID.randomUUID(), organisation.getId(), "owner@example.com",
                "hash", Role.ADMIN, true);
    }

    @Test void staffCannotDeleteAnItem() throws Exception
    {
        mockMvc.perform(post("/inventory/" + itemA.getId() + "/delete").with(user(staffOf(clinicA))).with(csrf()))
                .andExpect(status().isForbidden());

        assertNotNull(service.get(clinicA.getId(), itemA.getId()), "The item must still exist");
    }

    @Test void adminCanDeleteTheirOrganisationsItem() throws Exception
    {
        mockMvc.perform(post("/inventory/" + itemA.getId() + "/delete").with(user(adminOf(clinicA))).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThrows(InventoryItemNotFoundException.class, () -> service.get(clinicA.getId(), itemA.getId()));
    }

    @Test void adminCannotDeleteAnotherOrganisationsItem() throws Exception
    {
        mockMvc.perform(post("/inventory/" + itemB.getId() + "/delete").with(user(adminOf(clinicA))).with(csrf()))
                .andExpect(status().isNotFound());

        assertNotNull(service.get(clinicB.getId(), itemB.getId()), "The other organisation's item must remain");
    }
}