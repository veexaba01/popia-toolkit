package com.veex.popia_toolkit.auth;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HomeControllerTest
{
    private final HomeController controller = new HomeController();

    @Test void showsTheLoggedInEmailAndRole()
    {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "owner@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.home(authentication, model);

        // Assert
        assertEquals("home", view);
        assertEquals("owner@example.com", model.getAttribute("email"));
        assertEquals(List.of("ADMIN"), model.getAttribute("roles"));
    }
}