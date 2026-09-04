package com.arinno.canopus.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.ProjectMapper;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.ProjectRequest;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

@ExtendWith(MockitoExtension.class)
class ProjectControllerSecurityTests {

    @Mock
    private IProjectService projectService;

    @Mock
    private IProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ProjectMapper projectMapper;

    @Test
    void createRejectsProductFromAnotherCompany() throws Exception {
        Company company = new Company();
        company.setId(1L);
        Product foreignProduct = new Product();
        foreignProduct.setId(2L);
        ProjectRequest request = new ProjectRequest();
        request.setProductId(foreignProduct.getId());
        request.setResponsibleId(3L);

        when(jwtService.getCompanyFromToken("Bearer token")).thenReturn(company);
        when(productService.findByIdAndCompany(2L, company)).thenReturn(new Product());

        ProjectController controller = new ProjectController(projectService, jwtService, productService, userService, projectMapper);

        assertThatThrownBy(() -> controller.create(request, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
