package com.arinno.canopus.entities;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImputationResponse {

    private Long id;			
	private Date date;	
	private List<ImputationItemResponse> items;
    private Integer time; 

}
