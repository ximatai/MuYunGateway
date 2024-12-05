package net.ximatai.muyun.gateway.auth;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class SimpleSecurityContext implements SecurityContext {
    private final String username;
    private final String role;
    private final boolean isSecure;

    public SimpleSecurityContext(String username, String role, boolean isSecure) {
        this.username = username;
        this.role = role;
        this.isSecure = isSecure;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.role != null && this.role.equals(role);
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "CUSTOM_AUTH";
    }
}
