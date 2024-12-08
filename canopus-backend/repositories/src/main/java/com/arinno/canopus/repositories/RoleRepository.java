package com.arinno.canopus.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
