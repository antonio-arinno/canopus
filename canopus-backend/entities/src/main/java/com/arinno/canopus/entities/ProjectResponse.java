package com.arinno.canopus.entities;

import java.time.LocalDate;
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
