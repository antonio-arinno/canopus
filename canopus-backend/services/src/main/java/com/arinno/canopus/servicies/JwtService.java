package com.arinno.canopus.servicies;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.User;

public interface JwtService {

    Company getCompanyFromToken(String authorizationHeader);

    User getUserFromToken(String authorizationHeader);

}
