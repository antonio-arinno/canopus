package com.arinno.canopus.catalog.product.infrastructure.persistence;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;

public interface ProductRepository extends CrudRepository<Product, Long> {

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    Page<Product> findByCompany(Company company, Pageable pageable);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    Page<Product> findByResponsibleAndCompany(User user, Company company, Pageable pageable);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    public List<Product> findByCompany(Company company);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    public List<Product> findByResponsibleAndCompany(User user, Company company);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    @Query("select p from Product p where (p.responsible.id = ?1 or p.backup.id = ?1) and p.company.id = ?2")
    public List<Product> findByResponsibleOrBackupAndCompany(Long userId, Long companyId);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    public List<Product> findByTechnologyAndCompany(Technology technology, Company company);

    Optional<Product> findByIdAndCompany(Long id, Company company);

    public void deleteByIdAndCompany(Long id, Company company);

    @EntityGraph(attributePaths = {"technology", "responsible", "backup"})
    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public Integer countByResponsible(User responsible);

    public Integer countByTechnology(Technology technology);

}
