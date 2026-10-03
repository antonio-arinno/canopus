package com.arinno.canopus.services;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;

public interface CurrentUserContext {

    User getCurrentUser();

    Company getCurrentCompany();
}