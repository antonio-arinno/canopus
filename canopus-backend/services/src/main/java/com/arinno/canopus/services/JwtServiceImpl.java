package com.arinno.canopus.services;

import org.springframework.stereotype.Service;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;

@Service
public class JwtServiceImpl implements JwtService {

    private final CurrentUserContext currentUserContext;

    public JwtServiceImpl(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    // auth is no longer parsed here: identity comes from SecurityContext, populated by JwtValidationFilter.
    @Override
    public Company getCompanyFromToken(String auth) {
        return currentUserContext.getCurrentCompany();
    }

    @Override
    public User getUserFromToken(String auth) {
        return currentUserContext.getCurrentUser();
    }

}
