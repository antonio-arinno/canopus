package com.arinno.canopus.controllers.mapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.ProductResponse;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProjectService;

@Component
public class ProductMapper {

    private final IProjectService projectService;
    private final IImputationService imputationService;
    private final UserMapper userMapper;
    private final TechnologyMapper technologyMapper;

    public ProductMapper(IProjectService projectService, IImputationService imputationService,
            UserMapper userMapper, TechnologyMapper technologyMapper) {
        this.projectService = projectService;
        this.imputationService = imputationService;
        this.userMapper = userMapper;
        this.technologyMapper = technologyMapper;
    }

    public ProductResponse toIdName(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .build();
    }

    public ProductResponse toSummary(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .responsible(userMapper.toSummary(product.getResponsible()))
                .backup(userMapper.toSummary(product.getBackup()))
                .time(imputationService.timeByProduct(product))
                .build();
    }

    public ProductResponse toDetail(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .responsible(userMapper.toSummary(product.getResponsible()))
                .backup(userMapper.toSummary(product.getBackup()))
                .countProjects(projectService.countByProduct(product))
                .countContributors(projectService.countContributorsByProduct(product.getId()))
                .time(imputationService.timeByProduct(product))
                .avgTime(imputationService.avgTimeByProduct(product))
                .avgDuration(imputationService.avgDurationByProduct(product))
                .build();
    }

    public ProductResponse toResponseForUser(Product product, User user) {
        return ProductResponse.builder()
                .name(product.getName())
                .description(product.getDescription())
                .countProjects(projectService.countByProduct(product))
                .countContributors(projectService.countContributorsByProduct(product.getId()))
                .technology(TechnologyResponse.builder()
                        .name(product.getTechnology().getName())
                        .build())
                .responsible(UserResponse.builder()
                        .name(product.getResponsible().getName())
                        .build())
                .time(imputationService.timeByProductAndUser(product, user))
                .build();
    }
}
