package com.arinno.canopus.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Technology;

public interface TechnologyRepository extends CrudRepository<Technology, Long> {

    public List<Technology> findByCompany(Company company);

    Optional<Technology> findByIdAndCompany(Long id, Company company);

    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public void deleteByIdAndCompany(Long id, Company company);

}
