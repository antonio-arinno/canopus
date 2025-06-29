package com.arinno.canopus.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImputationItemResponse {

    private Long id;	
	private ProjectResponse project;
	private Integer time;	

}
