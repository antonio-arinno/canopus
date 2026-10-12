package com.arinno.canopus.organization.user.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.contract.ChangePasswordRequest;
import com.arinno.canopus.organization.user.domain.IUser;
import com.arinno.canopus.organization.user.domain.Role;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserListItem;
import com.arinno.canopus.organization.user.contract.UserProfileRequest;
import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.organization.user.infrastructure.persistence.RoleRepository;
import com.arinno.canopus.organization.user.infrastructure.persistence.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final UserTechnologyAssignments userTechnologyAssignments;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleRepository roleRepository,
            UserTechnologyAssignments userTechnologyAssignments) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userTechnologyAssignments = userTechnologyAssignments;
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
        return save(user, List.of());
    }

    @Transactional
    @Override
    public User save(User user, List<Long> technologyIds) {
        // 1. Validación defensiva del Username
        repository.findByUsername(user.getUsername()).ifPresent(existingUser -> {
            throw new UsernameAlreadyExistsException(
                "El nombre de usuario '" + user.getUsername() + "' ya se encuentra registrado en el sistema."
            );
        });

        // 2. Validación defensiva del Email
        repository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new EmailAlreadyExistsException(
                "El correo electrónico '" + user.getEmail() + "' ya se encuentra registrado en el sistema."
            );
        });

        user.setRoles(getRoles(user));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = repository.save(user);
        userTechnologyAssignments.replace(savedUser, technologyIds);
        return savedUser;
    }

    @Transactional
    @Override
    public User updateProfile(UserProfileRequest userDto, Long id) {
        Long userId = Objects.requireNonNull(id, "id must not be null");
        
        User userDb = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No se pudo actualizar el perfil, usuario no encontrado."));

        // 🔥 VALIDACIÓN DEFENSIVA: Buscamos si el nuevo email ya está en uso por OTRO usuario
        repository.findByEmail(userDto.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(userId)) {
                throw new EmailAlreadyExistsException(
                    "El correo electrónico '" + userDto.getEmail() + "' ya se encuentra registrado por otro usuario en el sistema."
                );
            }
        });                
        
        userDb.setEmail(userDto.getEmail());
        userDb.setLastname(userDto.getLastname());
        userDb.setName(userDto.getName());
        userTechnologyAssignments.replace(userDb, userDto.getTechnologies());
        
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

    @Override
    @Transactional(readOnly = true)
    public List<UserTechnologyResponse> findTechnologiesByUser(User user) {
        return userTechnologyAssignments.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<UserTechnologyResponse>> findTechnologiesByUsers(List<User> users) {
        return userTechnologyAssignments.findByUsers(users);
    }
}

