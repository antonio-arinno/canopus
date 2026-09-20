package com.arinno.canopus.controllers.mapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;
import com.arinno.canopus.entities.UserRequest;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;

@Component
public class UserMapper {

    private final IProductService productService;
    private final IProjectService projectService;
    private final IImputationService imputationService;
    private final TechnologyMapper technologyMapper;

    public UserMapper(IProductService productService, IProjectService projectService,
            IImputationService imputationService, TechnologyMapper technologyMapper) {
        this.productService = productService;
        this.projectService = projectService;
        this.imputationService = imputationService;
        this.technologyMapper = technologyMapper;
    }

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

    public UserResponse toDetail(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .lastname(user.getLastname())
                .technologies(user.getTechnologies().stream().map(technologyMapper::toIdName).toList())
                .countProducts(productService.countByResponsible(user))
                .countProjects(projectService.countByResponsible(user))
                .time(imputationService.timeByUser(user))
                .build();
    }

    public User toEntity(UserRequest request, Company company) {
        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setAdmin(request.isAdmin());
        user.setTechnologies(request.getTechnologies());
        user.setCompany(company);
        user.setPassword(request.getUsername());
        return user;
    }
}
