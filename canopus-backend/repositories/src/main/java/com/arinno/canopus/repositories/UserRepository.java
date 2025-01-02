package com.arinno.canopus.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;

public interface UserRepository extends CrudRepository<User, Long> {

//  Page<User> findAll(Pageable pageable);

    Optional<User> findByUsername(String name);

    List<User> findByCompany(Company company);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

}
