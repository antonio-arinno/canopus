package com.arinno.canopus.controllers.mapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.ProjectRequest;
import com.arinno.canopus.entities.ProjectResponse;
import com.arinno.canopus.servicies.IImputationService;

@Component
public class ProjectMapper {

    private final IImputationService imputationService;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final TechnologyMapper technologyMapper;

    public ProjectMapper(IImputationService imputationService, UserMapper userMapper,
            ProductMapper productMapper, TechnologyMapper technologyMapper) {
        this.imputationService = imputationService;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.technologyMapper = technologyMapper;
    }

    public ProjectResponse toSummary(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .dateDev(project.getDateDev())
                .datePre(project.getDatePre())
                .datePro(project.getDatePro())
                .responsible(userMapper.toIdName(project.getResponsible()))
                .product(productMapper.toIdName(project.getProduct()))
                .technology(technologyMapper.toIdName(project.getProduct().getTechnology()))
                .countContributors(project.getContributors().size())
                .time(imputationService.timeByProject(project))
                .build();
    }

    public ProjectResponse toDetail(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .reference1(project.getReference1())
                .reference2(project.getReference2())
                .dateDev(project.getDateDev())
                .datePre(project.getDatePre())
                .datePro(project.getDatePro())
                .contributors(project.getContributors().stream().map(userMapper::toSummary).toList())
                .responsible(userMapper.toIdName(project.getResponsible()))
                .product(productMapper.toIdName(project.getProduct()))
                .technology(technologyMapper.toIdName(project.getProduct().getTechnology()))
                .countContributors(project.getContributors().size())
                .time(imputationService.timeByProject(project))
                .build();
    }

    public Project toEntity(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setReference1(request.getReference1());
        project.setReference2(request.getReference2());
        project.setDateDev(request.getDateDev());
        project.setDatePre(request.getDatePre());
        project.setDatePro(request.getDatePro());
        return project;
    }
}
