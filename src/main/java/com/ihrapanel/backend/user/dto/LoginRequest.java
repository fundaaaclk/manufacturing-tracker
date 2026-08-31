package com.ihrapanel.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {


    @NotBlank(message = "Email boş olamaz.")
    @Email(message = "Geçerli bir email adresi giriniz.")
    private String email;

     @NotBlank(message = "Şifre boş olamaz.")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}