package com.ihrapanel.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface  UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);  // Email adresine göre kullanıcıyı bulur.
    // Kullanıcı bulunamayabileceği için Optional döner.
   List<User> findByCompanyId(UUID companyId);// Belirli bir şirkete ait tüm kullanıcıları getirir.
    // Spring Data JPA, User.company.id alanını kullanarak sorguyu otomatik oluşturur.
    boolean existsByEmail(String email);  // Verilen email adresiyle kayıtlı bir kullanıcı olup olmadığını kontrol eder.
}