//ilk kayıt sırasında şirket + OWNER oluşturmak için
package com.ihrapanel.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterUserRequest {

    // Şirket sahibinin adı
    @NotBlank(message = "İsim boş olamaz.")
    @Size(min = 2, max = 100, message = "İsim 2 ile 100 karakter arasında olmalıdır.")
    private String name;

     // OWNER'ın giriş yapacağı email
    @NotBlank(message = "Email boş olamaz.")
    @Email(message = "Geçerli bir email adresi giriniz.")
    private String email;

     @NotBlank(message = "Şifre boş olamaz.")
     @Size(min = 6 , max = 100, message = "Şifre en az 6 karakter olmalıdır.")
    private String password;

    // Oluşturulacak şirketin adı
    @NotBlank(message = "Şirket adı boş olamaz.")
    @Size(min = 2, max = 150, message = "Şirket adı 2 ile 150 karakter arasında olmalıdır.")
    private String companyName;

    
    private String taxNumber;

   
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

     public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

   
}