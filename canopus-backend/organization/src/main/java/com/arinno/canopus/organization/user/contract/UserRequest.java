package com.arinno.canopus.organization.user.contract;

import com.arinno.canopus.organization.user.domain.IUser;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserRequest implements IUser {

    private Long id; 

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotEmpty
    @Email
    private String email;

    @NotBlank
    @Size(min=4, max = 12)
    private String username;

    private boolean admin;

    private List<Long> technologies;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    
    
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<Long> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<Long> technologies) {
        this.technologies = technologies;
    }

    @Override
    public String toString() {
        return "UserRequest [id=" + id + ", name=" + name + ", lastname=" + lastname + ", email=" + email
                + ", username=" + username + ", admin=" + admin + ", technologies=" + technologies + "]";
    }


}
