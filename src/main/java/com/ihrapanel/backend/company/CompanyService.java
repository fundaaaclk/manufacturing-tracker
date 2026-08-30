package com.ihrapanel.backend.company;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // Yeni şirketi database'e kaydeder.
    public Company createCompany(Company company) {

        if (company.getTaxNumber() != null &&
                companyRepository.existsByTaxNumber(company.getTaxNumber())) {

            throw new IllegalArgumentException(
                    "Bu vergi numarasıyla kayıtlı bir şirket zaten var."
            );
        }

        return companyRepository.save(company);
    }

    // UUID ile şirketi bulur.
    public Optional<Company> findById(UUID id) {
        return companyRepository.findById(id);
    }

    // Vergi numarasıyla şirketi bulur.
    public Optional<Company> findByTaxNumber(String taxNumber) {
        return companyRepository.findByTaxNumber(taxNumber);
    }
}