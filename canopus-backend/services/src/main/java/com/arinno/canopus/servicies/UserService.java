package com.arinno.canopus.servicies;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserRequest;

public interface UserService {

//    List<User> findAll();

    List<User> findByCompany(Company company);

    Optional<User> findByUsername(String username);

//    Page<User> findAll(Pageable pageable);

    Optional<User> findById(@NonNull Long id);

    User save(User user);

    Optional<User> update(UserRequest user, Long id);

    void deleteById(Long id);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

}
