package com.arinno.canopus.organization.company.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.organization.company.domain.Company;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}