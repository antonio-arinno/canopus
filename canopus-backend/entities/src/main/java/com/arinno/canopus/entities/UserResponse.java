package com.arinno.canopus.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id; 
    private String username;
    private String name;
    private String lastname;
    private String email;
    private Integer countProducts;
    private Integer countProjects;
    private List<TechnologyResponse> technologies;
    private Integer time;
}
