package com.ihrapanel.backend.security;

import com.ihrapanel.backend.user.Role;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        UUID companyId,
        Role role
) {
}