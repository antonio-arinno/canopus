package com.arinno.canopus.servicies;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.User;

public interface CurrentUserContext {

    User getCurrentUser();

    Company getCurrentCompany();
}