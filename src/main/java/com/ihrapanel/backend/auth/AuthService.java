package com.ihrapanel.backend.auth;

import com.ihrapanel.backend.company.Company;
import com.ihrapanel.backend.company.CompanyRepository;
import com.ihrapanel.backend.security.JwtService;
import com.ihrapanel.backend.user.Role;
import com.ihrapanel.backend.user.User;
import com.ihrapanel.backend.user.UserRepository;
import com.ihrapanel.backend.user.dto.RegisterUserRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ihrapanel.backend.common.exception.ConflictException;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(RegisterUserRequest request) {

        // Email daha önce kullanılmış mı?
        String normalizedEmail =
                request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException(
                    "Bu email ile kayıtlı bir kullanıcı zaten var."
            );
        }

        // Vergi numarası daha önce kullanılmış mı?
        if (request.getTaxNumber() != null &&
                companyRepository.existsByTaxNumber(
                        request.getTaxNumber()
                )) {

            throw new ConflictException(
                    "Bu vergi numarasıyla kayıtlı bir şirket zaten var."
            );
        }

        // 1 - Yeni şirket oluştur
        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setTaxNumber(request.getTaxNumber());

        company = companyRepository.save(company);

        // 2 - Şirketin ilk OWNER'ını oluştur
        User owner = new User();

        owner.setName(request.getName());
        owner.setEmail(normalizedEmail);
        owner.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        // Kullanıcı role gönderemez.
        // İlk kullanıcı HER ZAMAN OWNER.
        owner.setRole(Role.OWNER);

        // Az önce oluşturduğumuz şirkete bağla.
        owner.setCompany(company);

        owner = userRepository.save(owner);

        // 3 - Kullanıcı direkt giriş yapmış olsun.
        return jwtService.generateToken(owner);
    }
}