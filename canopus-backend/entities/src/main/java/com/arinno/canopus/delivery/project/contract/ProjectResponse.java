package com.arinno.canopus.delivery.project.contract;



import com.arinno.canopus.delivery.project.domain.Status;

import com.arinno.canopus.catalog.product.contract.ProductResponse;

import java.time.LocalDate;
import java.util.List;

import com.arinno.canopus.organization.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.user.contract.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
	private String name;
	private String description;
    private String reference1;
    private String reference2;
    private LocalDate dateDev;
    private LocalDate datePre;
    private LocalDate datePro;
    private Status status;
    private UserResponse responsible;
    private List<UserResponse> contributors;
    private ProductResponse product;
    private TechnologyResponse technology;
    private Integer countContributors;
    private Integer time;
	


}
