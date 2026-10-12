package com.arinno.canopus.catalog.technology.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.catalog.technology.domain.UserTechnology;
import com.arinno.canopus.catalog.technology.domain.UserTechnologyId;

public interface UserTechnologyRepository extends CrudRepository<UserTechnology, UserTechnologyId> {

    @EntityGraph(attributePaths = "technology")
    List<UserTechnology> findByUser(User user);

    @EntityGraph(attributePaths = "technology")
    List<UserTechnology> findByUserIn(List<User> users);

    @Modifying
    @Query("delete from UserTechnology association where association.user = :user")
    int deleteByUser(@Param("user") User user);
}