package com.arinno.canopus.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.Technology;


public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String name);

    List<User> findByCompany(Company company);

    Optional<User> findByIdAndCompany(Long id, Company company);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public List<User> findByTechnologies(List<Technology> technologies);

/*
SELECT count(distinct contribuitor_id) FROM db_canopus.products product, db_canopus.projects project, db_canopus.projects_contributors project_contributor
where product.id = project.product_id
and project.id = project_contributor.project_id
and product.id = 4;
*/
}
