package com.arinno.canopus.time.imputation.contract;



import com.arinno.canopus.delivery.project.contract.ProjectResponse;

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
