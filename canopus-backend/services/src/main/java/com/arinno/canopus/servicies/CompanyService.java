package com.arinno.canopus.servicies;

import com.arinno.canopus.entities.Company;

public interface CompanyService {

    boolean existsByName(String name);

    Company save(Company company);

}
