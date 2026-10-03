package com.arinno.canopus.organization.user.contract;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class UserProfileRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotEmpty
    @Email
    private String email;

    private List<Long> technologies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Long> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<Long> technologies) {
        this.technologies = technologies;
    }
}
