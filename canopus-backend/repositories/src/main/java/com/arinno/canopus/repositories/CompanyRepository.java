package com.arinno.canopus.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

}
