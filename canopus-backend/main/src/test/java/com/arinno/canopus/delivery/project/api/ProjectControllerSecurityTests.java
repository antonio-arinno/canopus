package com.arinno.canopus.delivery.project.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.delivery.project.api.mapper.ProjectMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.delivery.project.application.IProjectService;
import com.arinno.canopus.services.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.delivery.project.contract.ProjectRequest;

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
        // Arrange (Preparación)
        Company company = new Company();
        company.setId(1L);
        
        ProjectRequest request = new ProjectRequest();
        request.setProductId(2L); // ID del producto de otra compañía
        request.setResponsibleId(3L);

        // Creamos mocks limpios para el test si no los tienes inyectados con @Mock
        org.mockito.MockitoAnnotations.openMocks(this); 

        when(jwtService.getCompanyFromToken("Bearer token")).thenReturn(company);
        
        // El servicio real ahora lanza un 404 (NOT_FOUND) si el producto no pertenece a la compañía
        when(productService.findByIdAndCompany(2L, company))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en esta empresa"));

        ProjectController controller = new ProjectController(projectService, jwtService, productService, userService, projectMapper);

        // Act & Assert (Acción y Verificación)
        assertThatThrownBy(() -> controller.create(request, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                // Verificamos que el sistema rechace con NOT_FOUND (404) gracias a la refactorización del servicio
                .isEqualTo(HttpStatus.NOT_FOUND);
    }


}
