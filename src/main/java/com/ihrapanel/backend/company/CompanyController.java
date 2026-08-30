package com.ihrapanel.backend.company;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(
            @RequestBody Company company
    ) {
        Company savedCompany = companyService.createCompany(company);

        return ResponseEntity.ok(savedCompany);
    }
}