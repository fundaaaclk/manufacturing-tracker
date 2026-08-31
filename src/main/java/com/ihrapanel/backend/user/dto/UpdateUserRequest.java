//update the informaiton about the  user 
package com.ihrapanel.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {


    @NotBlank(message = "İsim boş olamaz.")
    @Size(
        min = 2,
        max = 100,
        message = "İsim 2 ile 100 karakter arasında olmalıdır."
    )
    private String name;

     @NotBlank(message = "Email boş olamaz.")
    @Email(message = "Geçerli bir email adresi giriniz.")
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}