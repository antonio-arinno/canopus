package com.arinno.canopus.organization.technology.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.technology.domain.UserTechnology;
import com.arinno.canopus.organization.technology.domain.UserTechnologyId;

public interface UserTechnologyRepository extends CrudRepository<UserTechnology, UserTechnologyId> {

    List<UserTechnology> findByUser(User user);

    @Modifying
    @Query("delete from UserTechnology association where association.user = :user")
    int deleteByUser(@Param("user") User user);
}