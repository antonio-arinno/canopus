package com.arinno.canopus.organization.technology.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyResponsibleResponse {

    private Long id;
    private String name;
    private String lastname;
}