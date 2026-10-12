package com.arinno.canopus.organization.company.application;

import com.arinno.canopus.organization.company.domain.Company;

public interface CompanyService {

    boolean existsByName(String name);

    Company save(Company company);
}