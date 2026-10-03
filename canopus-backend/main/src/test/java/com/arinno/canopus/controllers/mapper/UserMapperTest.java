package com.arinno.canopus.controllers.mapper;




import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.time.imputation.application.IImputationService;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.delivery.project.application.IProjectService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.organization.technology.api.mapper.TechnologyMapper;

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

    @Mock
    private UserService userService;

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

        UserMapper userMapper = new UserMapper(
            productService, projectService, imputationService, technologyMapper, userService);

        UserResponse userResponse = userMapper.toDetail(user);

        assertThat(userResponse.getCountProducts()).isEqualTo(3);
        assertThat(userResponse.getCountProjects()).isEqualTo(5);
        assertThat(userResponse.getTime()).isEqualTo(40);
    }
}
