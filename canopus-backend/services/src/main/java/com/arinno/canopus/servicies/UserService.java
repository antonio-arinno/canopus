package com.arinno.canopus.servicies;

import java.util.List;
import java.util.Optional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserRequest;

public interface UserService {

    Object registrarYVerificar = null;

    List<User> findByCompany(Company company);

    List<User> findByTechnologies(List<Technology> technologies);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User save(User user);

    Optional<User> update(UserRequest user, Long id);

    void deleteById(Long id);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    void registrarYVerificar();

}
