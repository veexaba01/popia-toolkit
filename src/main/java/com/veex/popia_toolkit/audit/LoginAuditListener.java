package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import com.veex.popia_toolkit.auth.AppUserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginAuditListener
{
    private final AuditService auditService;
    private final AppUserRepository users;

    public LoginAuditListener(AuditService auditService, AppUserRepository users)
    {
        this.auditService = auditService;
        this.users = users;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event)
    {
        if (event.getAuthentication().getPrincipal() instanceof AppUserPrincipal principal)
        {
            auditService.record(principal.getOrganisationId(), principal.getUsername(),
                    AuditAction.LOGIN_SUCCESS, null);
        }
    }

    @EventListener
    @Transactional(readOnly = true)
    public void onFailure(AbstractAuthenticationFailureEvent event)
    {
        String email = event.getAuthentication().getName().trim();

        users.findByEmailIgnoreCase(email).ifPresent(user ->
                auditService.record(user.getOrganisation().getId(), user.getEmail(),
                        AuditAction.LOGIN_FAILURE, null));
    }
}