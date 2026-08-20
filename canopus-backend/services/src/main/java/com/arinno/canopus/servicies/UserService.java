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

    Optional<User> findByIdAndCompany(Long id, Company company);

    User save(User user);

    Optional<User> update(UserRequest user, Long id, Company company);

    boolean deleteById(Long id, Company company);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    void registrarYVerificar();

}
