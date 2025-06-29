package com.arinno.canopus.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyResponse {

    private Long id;
	private String name;
	private String description;
	private UserResponse responsible;
	private Integer countProducts;
	private Integer countProjects;
	private Integer countContributors;
	private Integer time;

}
