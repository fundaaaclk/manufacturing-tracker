package com.ihrapanel.backend.company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
     Optional<Company> findByTaxNumber(String taxNumber);
       // Vergi numarasına göre şirketi bulur.
    // Şirket bulunamayabileceği için Optional döner.
    boolean existsByTaxNumber(String taxNumber);
    // Verilen vergi numarasıyla kayıtlı bir şirket olup olmadığını kontrol eder.   
}
