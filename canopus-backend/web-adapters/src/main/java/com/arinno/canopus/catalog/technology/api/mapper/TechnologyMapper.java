package com.arinno.canopus.catalog.technology.api.mapper;




import org.springframework.stereotype.Component;

import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.catalog.technology.contract.TechnologyRequest;
import com.arinno.canopus.catalog.technology.contract.TechnologyResponse;
import com.arinno.canopus.catalog.technology.contract.TechnologyResponsibleResponse;
import com.arinno.canopus.reporting.contract.TechnologyMetrics;

@Component
public class TechnologyMapper {

    public TechnologyResponse toIdName(Technology technology) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .build();
    }

    public TechnologyResponse toSummary(Technology technology, TechnologyMetrics metrics) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .description(technology.getDescription())
                .countProducts(metrics.countProducts())
                .countProjects(metrics.countProjects())
                .countContributors(metrics.countContributors())
                .time(metrics.time())
                .build();
    }

    public TechnologyResponse toDetail(Technology technology, TechnologyMetrics metrics) {
        return TechnologyResponse.builder()
                .id(technology.getId())
                .name(technology.getName())
                .description(technology.getDescription())
                .responsible(TechnologyResponsibleResponse.builder()
                        .id(technology.getResponsible().getId())
                        .name(technology.getResponsible().getName())
                        .lastname(technology.getResponsible().getLastname())
                        .build())
                .countProducts(metrics.countProducts())
                .countProjects(metrics.countProjects())
                .countContributors(metrics.countContributors())
                .time(metrics.time())
                .build();
    }

    public Technology toEntity(TechnologyRequest request) {
        Technology technology = new Technology();
        technology.setName(request.getName());
        technology.setDescription(request.getDescription());
        return technology;
    }
}
