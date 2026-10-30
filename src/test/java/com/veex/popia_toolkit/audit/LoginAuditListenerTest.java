package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.auth.AppUser;
import com.veex.popia_toolkit.auth.AppUserPrincipal;
import com.veex.popia_toolkit.auth.AppUserRepository;
import com.veex.popia_toolkit.auth.Role;
import com.veex.popia_toolkit.organisation.Organisation;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class LoginAuditListenerTest
{
    private final AuditService audit = mock(AuditService.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final LoginAuditListener listener = new LoginAuditListener(audit, users);
    private final UUID organisationId = UUID.randomUUID();

    @Test void recordsASuccessfulLoginForTheUsersOrganisation()
    {
        // Arrange
        AppUserPrincipal principal = new AppUserPrincipal(UUID.randomUUID(), organisationId,
                "owner@example.com", "hash", Role.ADMIN, true);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        // Act
        listener.onSuccess(event);

        // Assert
        verify(audit).record(organisationId, "owner@example.com", AuditAction.LOGIN_SUCCESS, null);
    }

    @Test void ignoresASuccessfulAuthenticationThatIsNotAnApplicationUser()
    {
        // Arrange
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken("someone", null));

        // Act
        listener.onSuccess(event);

        // Assert
        verifyNoInteractions(audit);
    }

    @Test void recordsAFailedLoginForAnExistingAccount()
    {
        // Arrange
        Organisation organisation = mock(Organisation.class);
        when(organisation.getId()).thenReturn(organisationId);
        AppUser user = mock(AppUser.class);
        when(user.getOrganisation()).thenReturn(organisation);
        when(user.getEmail()).thenReturn("owner@example.com");
        when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("owner@example.com", "wrong"),
                new BadCredentialsException("Bad credentials"));

        // Act
        listener.onFailure(event);

        // Assert
        verify(audit).record(organisationId, "owner@example.com", AuditAction.LOGIN_FAILURE, null);
    }

    @Test void doesNotRecordAFailedLoginForAnUnknownEmail()
    {
        // Arrange
        when(users.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("nobody@example.com", "wrong"),
                new BadCredentialsException("Bad credentials"));

        // Act
        listener.onFailure(event);

        // Assert
        verifyNoInteractions(audit);
    }
}