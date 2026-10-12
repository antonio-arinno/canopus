package com.arinno.canopus.organization.user.application;

import java.util.List;
import java.util.Map;

import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.organization.user.domain.User;

public interface UserTechnologyAssignments {

    void replace(User user, List<Long> technologyIds);

    List<UserTechnologyResponse> findByUser(User user);

    Map<Long, List<UserTechnologyResponse>> findByUsers(List<User> users);
}