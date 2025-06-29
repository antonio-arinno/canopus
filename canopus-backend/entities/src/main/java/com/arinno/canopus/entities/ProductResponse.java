package com.arinno.canopus.entities;

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
