package com.arinno.canopus.organization.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.organization.user.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
