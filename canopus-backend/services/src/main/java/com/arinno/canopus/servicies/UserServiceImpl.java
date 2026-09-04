package com.arinno.canopus.servicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.ChangePasswordRequest;
import com.arinno.canopus.entities.IUser;
import com.arinno.canopus.entities.Role;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;
import com.arinno.canopus.entities.UserProfileRequest;
import com.arinno.canopus.repositories.RoleRepository;
import com.arinno.canopus.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository repository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByCompany(Company company) {
        return this.repository.findByCompany(company); 
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByCompany(Company company, Pageable pageable) {
        return repository.findByCompany(company, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListItem> findListItemsByCompany(Company company, Pageable pageable) {
        return repository.findListItemsByCompany(company, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        return repository.findById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByIdAndCompany(Long id, Company company) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        return repository.findByIdAndCompany(userId, company);
    }

    @Transactional
    @Override
    public User save(User user) {
        user.setRoles(getRoles(user));       
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    
    @Transactional
    @Override
    public Optional<User> updateProfile(UserProfileRequest user, Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        
        Optional<User> userOptional = repository.findById(userId);
        
        if (userOptional.isPresent()) {
            User userDb = userOptional.get();
            userDb.setEmail(user.getEmail());
            userDb.setLastname(user.getLastname());
            userDb.setName(user.getName());
            userDb.setTechnologies(user.getTechnologies());
            return Optional.of(repository.save(userDb));
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public boolean changePassword(ChangePasswordRequest request, Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        Optional<User> userOptional = repository.findById(userId);
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getCurrentPassword(), userOptional.get().getPassword())) {
            return false;
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        return true;
    }
    
    @Transactional
    @Override
    public boolean deleteById(Long id, Company company) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        Optional<User> userOptional = repository.findByIdAndCompany(userId, company);
        if (userOptional.isEmpty()) {
            return false;
        }
        repository.delete(userOptional.get());
        return true;
    }
    
    private List<Role> getRoles(IUser user) {
        List<Role> roles = new ArrayList<>();
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        optionalRoleUser.ifPresent(roles::add);
    
        if(user.isAdmin()){
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            optionalRoleAdmin.ifPresent(roles::add);
        }

        return roles;
    }

    @Override
	@Transactional
	public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
		return repository.findByNameContainingIgnoreCaseAndCompany(term, company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByTechnologyAndCompany(Long technologyId, Long companyId) {
        return repository.findByTechnologyAndCompany(technologyId, companyId);
    }

}
