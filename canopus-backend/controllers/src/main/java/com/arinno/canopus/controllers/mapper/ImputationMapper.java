package com.arinno.canopus.controllers.mapper;

import org.springframework.stereotype.Component;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationItem;
import com.arinno.canopus.entities.ImputationItemResponse;
import com.arinno.canopus.entities.ImputationResponse;
import com.arinno.canopus.entities.ProjectResponse;

@Component
public class ImputationMapper {

    public ImputationResponse toResponse(Imputation imputation) {
        return ImputationResponse.builder()
                .id(imputation.getId())
                .date(imputation.getDate())
                .time(imputation.getTotal())
                .build();
    }

    public ImputationResponse toDetailResponse(Imputation imputation) {
        return ImputationResponse.builder()
                .id(imputation.getId())
                .date(imputation.getDate())
                .time(imputation.getTotal())
                .items(imputation.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    public ImputationItemResponse toItemResponse(ImputationItem item) {
        return ImputationItemResponse.builder()
                .id(item.getId())
                .time(item.getTime())
                .project(ProjectResponse.builder()
                        .id(item.getProject().getId())
                        .name(item.getProject().getName())
                        .build())
                .build();
    }
}
