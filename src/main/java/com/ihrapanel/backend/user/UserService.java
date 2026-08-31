package com.ihrapanel.backend.user;

import com.ihrapanel.backend.company.Company;
import com.ihrapanel.backend.company.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.ihrapanel.backend.common.exception.ConflictException;
import com.ihrapanel.backend.common.exception.ResourceNotFoundException;


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

    // OWNER bu endpoint üzerinden başka OWNER oluşturamaz.-400
    if (role == Role.OWNER) {
        throw new IllegalArgumentException(
                "Yeni OWNER bu endpoint üzerinden oluşturulamaz."
        );
    }

    String normalizedEmail = email.trim().toLowerCase();
//eğer email abskası tarafından kullanılıyorsa-409
    if (userRepository.existsByEmail(normalizedEmail)) {
        throw new ConflictException(
                "Bu email ile kayıtlı bir kullanıcı zaten var."
        );
    }
//şirket yok 404
    Company company = companyRepository.findById(companyId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Şirket bulunamadı.")
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
                    new ResourceNotFoundException("Kullanıcı bulunamadı.")
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
                    new ResourceNotFoundException("Kullanıcı bulunamadı.")
            );

    String normalizedEmail = email.trim().toLowerCase();

    if (!user.getEmail().equals(normalizedEmail)
            && userRepository.existsByEmail(normalizedEmail)) {

        throw new ConflictException(
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
                    new ResourceNotFoundException("Kullanıcı bulunamadı.")
            );

    user.setActive(active);

    return userRepository.save(user);
}

}