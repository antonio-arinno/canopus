package com.arinno.canopus.organization.user.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.contract.ChangePasswordRequest;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserListItem;
import com.arinno.canopus.organization.user.contract.UserProfileRequest;
import com.arinno.canopus.organization.technology.domain.Technology;

public interface UserService {

    List<User> findByCompany(Company company);

    Page<User> findByCompany(Company company, Pageable pageable);

    Page<UserListItem> findListItemsByCompany(Company company, Pageable pageable);

    List<User> findByTechnologyAndCompany(Long technologyId, Long companyId);

    List<Technology> findTechnologiesByUser(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User findByIdAndCompany(Long id, Company company);

    User save(User user);

    User save(User user, List<Long> technologyIds);

    User updateProfile(UserProfileRequest user, Long id);

    void changePassword(ChangePasswordRequest request, Long id);

    void deleteById(Long id, Company company);

    List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);
}
