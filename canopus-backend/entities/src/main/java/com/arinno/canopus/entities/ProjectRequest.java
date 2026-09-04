package com.arinno.canopus.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProjectRequest {

    @NotBlank
    private String name;

    private String description;
    private String reference1;
    private String reference2;

    @NotNull
    private LocalDate dateDev;

    private LocalDate datePre;
    private LocalDate datePro;

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Long responsibleId;

    private List<@Positive Long> contributorIds = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReference1() { return reference1; }
    public void setReference1(String reference1) { this.reference1 = reference1; }
    public String getReference2() { return reference2; }
    public void setReference2(String reference2) { this.reference2 = reference2; }
    public LocalDate getDateDev() { return dateDev; }
    public void setDateDev(LocalDate dateDev) { this.dateDev = dateDev; }
    public LocalDate getDatePre() { return datePre; }
    public void setDatePre(LocalDate datePre) { this.datePre = datePre; }
    public LocalDate getDatePro() { return datePro; }
    public void setDatePro(LocalDate datePro) { this.datePro = datePro; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getResponsibleId() { return responsibleId; }
    public void setResponsibleId(Long responsibleId) { this.responsibleId = responsibleId; }
    public List<Long> getContributorIds() { return contributorIds; }
    public void setContributorIds(List<Long> contributorIds) {
        this.contributorIds = contributorIds == null ? new ArrayList<>() : new ArrayList<>(contributorIds);
    }
}