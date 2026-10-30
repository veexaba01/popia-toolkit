package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
public class HomePageTest
{
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void adminSeesTheAdminLinks() throws Exception
    {
        String page = mockMvc.perform(get("/").with(user("owner@example.com").roles("ADMIN")))
                .andReturn().getResponse().getContentAsString();

        assertTrue(page.contains("/admin/staff"), "An admin should see the add staff link");
        assertTrue(page.contains("/admin/audit"), "An admin should see the audit log link");
    }

    @Test void staffDoNotSeeAnyAdminLinks() throws Exception
    {
        String page = mockMvc.perform(get("/").with(user("staff@example.com").roles("STAFF")))
                .andReturn().getResponse().getContentAsString();

        assertFalse(page.contains("/admin/"), "Staff should not be shown admin links");
    }
}