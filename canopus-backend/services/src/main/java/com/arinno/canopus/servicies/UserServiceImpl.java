package com.arinno.canopus.servicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.IUser;
import com.arinno.canopus.entities.Role;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserRequest;
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
/*
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List) this.repository.findAll();
    }
 */
    @Override
    @Transactional(readOnly = true)
    public List<User> findByCompany(Company company) {
        return (List<User>) this.repository.findByCompany(company); 
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return this.repository.findByUsername(username);
    }

/*
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return this.repository.findAll(pageable);
    }
 */
    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(@NonNull Long id) {
        return repository.findById(id);
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
    public Optional<User> update(UserRequest user, Long id) {
        
        Optional<User> userOptional = repository.findById(id);
        
        if (userOptional.isPresent()) {
            User userDb = userOptional.get();
            userDb.setEmail(user.getEmail());
            userDb.setLastname(user.getLastname());
            userDb.setName(user.getName());
            userDb.setUsername(user.getUsername());
            userDb.setRoles(getRoles(user));
            return Optional.of(repository.save(userDb));
        }
        return Optional.empty();
    }
    
    @Transactional
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    private List<Role> getRoles(IUser user) {
        List<Role> roles = new ArrayList<>();
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
    //        optionalRoleUser.ifPresent(role -> roles.add(role));
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


}
