package com.arinno.canopus.catalog.technology.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.technology.domain.Technology;

public interface TechnologyRepository extends CrudRepository<Technology, Long> {

    @EntityGraph(attributePaths = "responsible")
    Page<Technology> findByCompany(Company company, Pageable pageable);

    @EntityGraph(attributePaths = "responsible")
    public List<Technology> findByCompany(Company company);

    Optional<Technology> findByIdAndCompany(Long id, Company company);

    @EntityGraph(attributePaths = "responsible")
    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public void deleteByIdAndCompany(Long id, Company company);

}
