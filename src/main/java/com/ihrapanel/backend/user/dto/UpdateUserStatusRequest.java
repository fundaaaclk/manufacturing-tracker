//useri aktif/değil gibisinden
package com.ihrapanel.backend.user.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "Aktiflik durumu belirtilmelidir.")
    private Boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}