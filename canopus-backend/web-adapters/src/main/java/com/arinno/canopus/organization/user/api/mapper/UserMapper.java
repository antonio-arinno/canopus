package com.arinno.canopus.organization.user.api.mapper;

import java.util.List;




import org.springframework.stereotype.Component;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserListItem;
import com.arinno.canopus.organization.user.contract.UserRequest;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.reporting.contract.UserMetrics;

@Component
public class UserMapper {

    public UserResponse toIdName(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public UserResponse toSummary(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .build();
    }

    public UserResponse toListResponse(UserListItem user) {
        return UserResponse.builder()
                .id(user.id())
                .username(user.username())
                .name(user.name())
                .lastname(user.lastname())
                .countProducts(user.countProducts().intValue())
                .time(user.time().intValue())
                .build();
    }

    public UserResponse toDetail(User user, UserMetrics metrics, List<UserTechnologyResponse> technologies) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .lastname(user.getLastname())
                .technologies(technologies)
                .countProducts(metrics.countProducts())
                .countProjects(metrics.countProjects())
                .time(metrics.time())
                .build();
    }

    public User toEntity(UserRequest request, Company company) {
        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setAdmin(request.isAdmin());
        user.setCompany(company);
        user.setPassword(request.getUsername());
        return user;
    }
}
