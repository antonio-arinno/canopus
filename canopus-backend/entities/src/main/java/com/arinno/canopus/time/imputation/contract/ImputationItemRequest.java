package com.arinno.canopus.time.imputation.contract;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ImputationItemRequest {

    @NotNull
    @Positive
    private Long projectId;

    @NotNull
    @Positive
    private Integer time;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
