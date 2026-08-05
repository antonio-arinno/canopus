package com.arinno.canopus.servicies;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;

public interface JwtService {

    Company getCompanyFromToken(String authorizationHeader);

    User getUserFromToken(String authorizationHeader);

}
