package com.ihrapanel.backend.user;

import com.ihrapanel.backend.company.Company;
import com.ihrapanel.backend.company.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Yeni kullanici kaydi. rawPassword = kullanicinin girdigi duz metin sifre -
    // bunu ASLA oldugu gibi kaydetmiyoruz, once hash'liyoruz.
    //patron oluşturulken eskimethos (patron warehouse ve muhabaseciyi olusturuken createEmployee kullanıcak)
    public User registerUser(UUID companyId, String name, String email,
                              String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Bu email ile kayitli bir kullanici zaten var."
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sirket bulunamadi."
                ));

        User user = new User();
        user.setName(name);
        user.setEmail(normalizedEmail); 
        
        // encode(): rawPassword'u BCrypt hash'ine cevirir. Ornegin
        // "sifre123" -> "$2a$10$N9qo8uLOickgx2ZMRZoMy..." gibi bir sey olur.
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setCompany(company);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
      return userRepository.findByEmail(
            email.trim().toLowerCase()
      );
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByCompany(UUID companyId) {
    return userRepository.findByCompanyId(companyId);
}
  


public User createEmployee(
        UUID companyId,
        String name,
        String email,
        String rawPassword,
        Role role
) {

    // OWNER bu endpoint üzerinden başka OWNER oluşturamaz.
    if (role == Role.OWNER) {
        throw new IllegalArgumentException(
                "Yeni OWNER bu endpoint üzerinden oluşturulamaz."
        );
    }

    String normalizedEmail = email.trim().toLowerCase();

    if (userRepository.existsByEmail(normalizedEmail)) {
        throw new IllegalArgumentException(
                "Bu email ile kayıtlı bir kullanıcı zaten var."
        );
    }

    Company company = companyRepository.findById(companyId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Şirket bulunamadı.")
            );

    User user = new User();
    user.setName(name);
    user.setEmail(normalizedEmail);
    user.setPasswordHash(
            passwordEncoder.encode(rawPassword)
    );
    user.setRole(role);
    user.setCompany(company);

    return userRepository.save(user);
}

//userid ile getirme ayrıcıa companyid de var 
public User getUserById(UUID userId, UUID companyId) {
    return userRepository.findByIdAndCompanyId(userId, companyId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Kullanıcı bulunamadı.")
            );
}


//update the users information(email and name)
public User updateUser(
        UUID userId,
        UUID companyId,
        String name,
        String email
) {
    User user = userRepository
            .findByIdAndCompanyId(userId, companyId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Kullanıcı bulunamadı.")
            );

    String normalizedEmail = email.trim().toLowerCase();

    if (!user.getEmail().equals(normalizedEmail)
            && userRepository.existsByEmail(normalizedEmail)) {

        throw new IllegalArgumentException(
                "Bu email ile kayıtlı bir kullanıcı zaten var."
        );
    }

    user.setName(name);
    user.setEmail(normalizedEmail);

    return userRepository.save(user);
}

//userin aktif/aktif değil olayıı yapıcaz
public User changeUserActiveStatus(
        UUID userId,
        UUID companyId,
        boolean active
) {
    User user = userRepository
            .findByIdAndCompanyId(userId, companyId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Kullanıcı bulunamadı.")
            );

    user.setActive(active);

    return userRepository.save(user);
}

}