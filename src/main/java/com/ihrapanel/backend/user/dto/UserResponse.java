package com.ihrapanel.backend.user.dto;

import com.ihrapanel.backend.user.Role;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private UUID companyId;

    public UserResponse(UUID id, String name, String email,
                        Role role, UUID companyId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.companyId = companyId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UUID getCompanyId() {
        return companyId;
    }
}