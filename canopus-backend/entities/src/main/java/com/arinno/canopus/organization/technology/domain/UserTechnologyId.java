package com.arinno.canopus.organization.technology.domain;

import java.io.Serializable;
import java.util.Objects;

public class UserTechnologyId implements Serializable {

    private Long user;
    private Long technology;

    public UserTechnologyId() {
    }

    public UserTechnologyId(Long user, Long technology) {
        this.user = user;
        this.technology = technology;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserTechnologyId that)) {
            return false;
        }
        return Objects.equals(user, that.user) && Objects.equals(technology, that.technology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, technology);
    }
}