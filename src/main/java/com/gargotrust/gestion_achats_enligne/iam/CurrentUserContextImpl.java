package com.gargotrust.gestion_achats_enligne.iam;

import com.gargotrust.gestion_achats_enligne.iam.security.UserPrincipal;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

@Component
@RequestScope
public class CurrentUserContextImpl implements CurrentUserContext {

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private UserPrincipal principal() {
        Authentication auth = auth();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) return p;
        return null;
    }

    @Override
    public UUID getAccountId() {
        UserPrincipal p = principal();
        return p != null ? p.getAccountId() : null;
    }

    @Override
    public String getEmail() {
        UserPrincipal p = principal();
        return p != null ? p.getEmail() : null;
    }

    @Override
    public String getRole() {
        UserPrincipal p = principal();
        return p != null ? p.getRole() : null;
    }

    @Override
    public List<String> getPermissions() {
        UserPrincipal p = principal();
        return p != null ? p.getPermissions() : List.of();
    }

    @Override
    public boolean hasRole(String role) {
        Authentication auth = auth();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = auth();
        return auth != null && auth.isAuthenticated() && principal() != null;
    }
}
