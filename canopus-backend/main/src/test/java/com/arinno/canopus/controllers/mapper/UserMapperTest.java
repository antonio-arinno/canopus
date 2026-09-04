package com.arinno.canopus.controllers.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private IProductService productService;

    @Mock
    private IProjectService projectService;

    @Mock
    private IImputationService imputationService;

    @Mock
    private TechnologyMapper technologyMapper;

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

        when(productService.countByResponsible(user)).thenReturn(3);
        when(projectService.countByResponsible(user)).thenReturn(5);
        when(imputationService.timeByUser(user)).thenReturn(40);

        UserMapper userMapper = new UserMapper(productService, projectService, imputationService, technologyMapper);

        UserResponse userResponse = userMapper.toDetail(user);

        assertThat(userResponse.getCountProducts()).isEqualTo(3);
        assertThat(userResponse.getCountProjects()).isEqualTo(5);
        assertThat(userResponse.getTime()).isEqualTo(40);
    }
}
