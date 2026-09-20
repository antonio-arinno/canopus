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

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.ChangePasswordRequest;
import com.arinno.canopus.entities.IUser;
import com.arinno.canopus.entities.Role;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;
import com.arinno.canopus.entities.UserProfileRequest;
import com.arinno.canopus.error.InvalidPasswordException;
import com.arinno.canopus.error.UserNotFoundException;
import com.arinno.canopus.repositories.RoleRepository;
import com.arinno.canopus.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
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

    @Override
    @Transactional(readOnly = true)
    public User findByIdAndCompany(Long id, Company company) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        return repository.findByIdAndCompany(userId, company)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con el id: " + userId));
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
    public User updateProfile(UserProfileRequest userDto, Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        
        User userDb = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No se pudo actualizar el perfil, usuario no encontrado."));
        
        userDb.setEmail(userDto.getEmail());
        userDb.setLastname(userDto.getLastname());
        userDb.setName(userDto.getName());
        userDb.setTechnologies(userDto.getTechnologies());
        
        return repository.save(userDb);
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request, Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual no es válida.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    @Transactional
    @Override
    public void deleteById(Long id, Company company) {
        User user = repository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new UserNotFoundException("No se encontró el usuario a eliminar con id: " + id));
        repository.delete(user);
    }
    
    private List<Role> getRoles(IUser user) {
        List<Role> roles = new ArrayList<>();
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        optionalRoleUser.ifPresent(roles::add);
    
        if (user.isAdmin()) {
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

