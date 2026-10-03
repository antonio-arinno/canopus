package com.arinno.canopus.organization.technology.domain;

import java.util.Objects;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@IdClass(UserTechnologyId.class)
@Table(name = "users_technologies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_technologies", columnNames = {"user_id", "technology_id"})
})
public class UserTechnology {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @PrePersist
    @PreUpdate
    private void validateCompany() {
        if (user == null || technology == null || company == null
            || user.getCompany() == null
                || !Objects.equals(company.getId(), user.getCompany().getId())
            || (technology.getCompany() != null
                && !Objects.equals(company.getId(), technology.getCompany().getId()))) {
            throw new IllegalStateException("User-technology association must belong to the same company");
        }
    }
}