package com.arinno.canopus.organization.technology.api.mapper;




import org.springframework.stereotype.Component;

import com.arinno.canopus.organization.technology.domain.Technology;
import com.arinno.canopus.organization.technology.contract.TechnologyRequest;
import com.arinno.canopus.organization.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.technology.contract.TechnologyResponsibleResponse;
import com.arinno.canopus.time.imputation.application.IImputationService;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.delivery.project.application.IProjectService;

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
                .responsible(TechnologyResponsibleResponse.builder()
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
