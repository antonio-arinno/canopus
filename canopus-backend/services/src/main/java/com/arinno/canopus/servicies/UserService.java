package com.arinno.canopus.servicies;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.ChangePasswordRequest;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;
import com.arinno.canopus.entities.UserProfileRequest;

public interface UserService {

    Object registrarYVerificar = null;

    List<User> findByCompany(Company company);

    Page<User> findByCompany(Company company, Pageable pageable);

    Page<UserListItem> findListItemsByCompany(Company company, Pageable pageable);

    List<User> findByTechnologies(List<Technology> technologies);

    List<User> findByTechnologyAndCompany(Long technologyId, Long companyId);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Optional<User> findByIdAndCompany(Long id, Company company);

    User save(User user);

    Optional<User> updateProfile(UserProfileRequest user, Long id);

    boolean changePassword(ChangePasswordRequest request, Long id);

    boolean deleteById(Long id, Company company);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    void registrarYVerificar();

}
