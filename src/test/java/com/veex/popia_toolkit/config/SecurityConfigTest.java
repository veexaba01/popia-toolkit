package com.veex.popia_toolkit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
public class SecurityConfigTest
{
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void registrationPageIsPublic() throws Exception
    {
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test void otherPagesRedirectAnonymousVisitorsToLogin() throws Exception
    {
        mockMvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }

    @Test void postWithoutCsrfTokenIsForbidden() throws Exception
    {
        mockMvc.perform(post("/register")).andExpect(status().isForbidden());
    }

        @Test void staffCannotReachAdminPages() throws Exception
    {
        mockMvc.perform(get("/admin/staff").with(user("staff@example.com").roles("STAFF")))
                .andExpect(status().isForbidden());
    }
}