package com.arinno.canopus.delivery.project.api.mapper;



import com.arinno.canopus.catalog.product.api.mapper.ProductMapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.delivery.project.contract.ProjectRequest;
import com.arinno.canopus.delivery.project.contract.ProjectResponse;
import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.catalog.technology.api.mapper.TechnologyMapper;
import com.arinno.canopus.reporting.contract.ProjectMetrics;

@Component
public class ProjectMapper {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final TechnologyMapper technologyMapper;

    public ProjectMapper(UserMapper userMapper,
            ProductMapper productMapper, TechnologyMapper technologyMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.technologyMapper = technologyMapper;
    }

    public ProjectResponse toSummary(Project project, ProjectMetrics metrics) {
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
                .countContributors(metrics.countContributors())
                .time(metrics.time())
                .build();
    }

    public ProjectResponse toDetail(Project project, ProjectMetrics metrics) {
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
                .countContributors(metrics.countContributors())
                .time(metrics.time())
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
