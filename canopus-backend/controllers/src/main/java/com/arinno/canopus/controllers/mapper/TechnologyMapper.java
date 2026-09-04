package com.arinno.canopus.controllers.mapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyRequest;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;

@Component
public class TechnologyMapper {

    private final IProductService productService;
    private final IProjectService projectService;
    private final IImputationService imputationService;

    public TechnologyMapper(IProductService productService, IProjectService projectService,
            IImputationService imputationService) {
        this.productService = productService;
        this.projectService = projectService;
        this.imputationService = imputationService;
    }

    public TechnologyResponse toIdName(Technology technology) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .build();
    }

    public TechnologyResponse toSummary(Technology technology) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .description(technology.getDescription())
                .countProducts(productService.countByTechnology(technology))
                .countProjects(projectService.countByTechnology(technology.getId()))
                .countContributors(projectService.countContributorsByTechnology(technology.getId()))
                .time(imputationService.timeByTechnology(technology))
                .build();
    }

    public TechnologyResponse toDetail(Technology technology) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .description(technology.getDescription())
                .responsible(UserResponse.builder()
                        .id(technology.getResponsible().getId())
                        .name(technology.getResponsible().getName())
                        .lastname(technology.getResponsible().getLastname())
                        .build())
                .countProducts(productService.countByTechnology(technology))
                .countProjects(projectService.countByTechnology(technology.getId()))
                .countContributors(projectService.countContributorsByTechnology(technology.getId()))
                .time(imputationService.timeByTechnology(technology))
                .build();
    }

    public Technology toEntity(TechnologyRequest request) {
        Technology technology = new Technology();
        technology.setName(request.getName());
        technology.setDescription(request.getDescription());
        return technology;
    }
}
