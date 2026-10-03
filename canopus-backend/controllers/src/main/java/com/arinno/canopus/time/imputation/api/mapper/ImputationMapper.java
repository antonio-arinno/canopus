package com.arinno.canopus.time.imputation.api.mapper;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.arinno.canopus.time.imputation.domain.Imputation;
import com.arinno.canopus.time.imputation.domain.ImputationItem;
import com.arinno.canopus.time.imputation.contract.ImputationItemResponse;
import com.arinno.canopus.time.imputation.contract.ImputationRequest;
import com.arinno.canopus.time.imputation.contract.ImputationResponse;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.delivery.project.contract.ProjectResponse;

@Component
public class ImputationMapper {

    public Imputation toEntity(ImputationRequest request, List<Project> projects) {
        Imputation imputation = new Imputation();
        imputation.setDate(Date.valueOf(request.getDate()));
        imputation.setItems(request.getItems().stream().map(itemRequest -> {
            Project project = projects.stream()
                    .filter(candidate -> candidate.getId().equals(itemRequest.getProjectId()))
                    .findFirst()
                    .orElseThrow();
            ImputationItem item = new ImputationItem();
            item.setProject(project);
            item.setTime(itemRequest.getTime());
            return item;
        }).toList());
        return imputation;
    }

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
