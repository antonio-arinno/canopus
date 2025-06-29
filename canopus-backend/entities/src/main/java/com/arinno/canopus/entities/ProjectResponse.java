package com.arinno.canopus.entities;

import java.util.Date;
import java.util.List;

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
    private Date dateDev;
    private Date datePre;
    private Date datePro;
    private Status status;
    private UserResponse responsible;
    private List<UserResponse> contributors;
    private ProductResponse product;
    private TechnologyResponse technology;
    private Integer countContributors;
    private Integer time;
	


}
