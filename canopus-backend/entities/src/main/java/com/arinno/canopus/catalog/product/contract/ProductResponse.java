package com.arinno.canopus.catalog.product.contract;


import com.arinno.canopus.organization.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.user.contract.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class ProductResponse {

    private Long id;
	private String name;
	private String description;
    private TechnologyResponse technology;
    private UserResponse responsible;
    private UserResponse backup;
    private Integer countProjects;
    private Integer countContributors;
    private Integer time;
    private Float avgTime;
    private Integer avgDuration;

}
