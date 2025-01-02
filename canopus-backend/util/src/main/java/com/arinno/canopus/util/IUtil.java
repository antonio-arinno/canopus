package com.arinno.canopus.util;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;

public interface IUtil {

    public Company getCompany(String authentication);

    public User getUser(String authentication);

}
