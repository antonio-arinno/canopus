package com.arinno.canopus.security;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;

public interface JwtService {

    Company getCompanyFromToken(String authorizationHeader);

    User getUserFromToken(String authorizationHeader);

}
