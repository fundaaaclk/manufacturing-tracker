//Owner useri create ederken bu olucak

package com.ihrapanel.backend.user.dto;

import com.ihrapanel.backend.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class CreateUserRequest {

    @NotBlank(message = "İsim boş olamaz.")
    @Size(min = 2, max = 100, message = "İsim 2 ile 100 karakter arasında olmalıdır.")
    private String name;

      @NotBlank(message = "Email boş olamaz.")
    @Email(message = "Geçerli bir email adresi giriniz.")
    private String email;

       @NotBlank(message = "Şifre boş olamaz.")
    @Size(min = 6, max = 100, message = "Şifre en az 6 karakter olmalıdır.")
    private String password;

    @NotNull(message = "Rol seçilmelidir.")
    private Role role;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}