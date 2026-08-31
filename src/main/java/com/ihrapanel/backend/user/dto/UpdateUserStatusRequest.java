//useri aktif/değil gibisinden
package com.ihrapanel.backend.user.dto;

public class UpdateUserStatusRequest {

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}