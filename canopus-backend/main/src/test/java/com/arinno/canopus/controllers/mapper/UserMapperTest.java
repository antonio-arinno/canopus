package com.arinno.canopus.controllers.mapper;




import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.reporting.contract.UserMetrics;

class UserMapperTest {

    @Test
    void toDetailIncludesCountProductsCountProjectsAndTime() {
        Company company = new Company();
        company.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setName("Test");
        user.setLastname("User");
        user.setEmail("test@example.com");
        user.setCompany(company);

        UserMapper userMapper = new UserMapper();

        UserResponse userResponse = userMapper.toDetail(user, new UserMetrics(3, 5, 40), java.util.List.of());

        assertThat(userResponse.getCountProducts()).isEqualTo(3);
        assertThat(userResponse.getCountProjects()).isEqualTo(5);
        assertThat(userResponse.getTime()).isEqualTo(40);
    }
}
