package com.arinno.canopus.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.catalog.technology.api.mapper.TechnologyMapper;
import com.arinno.canopus.catalog.technology.api.TechnologyController;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.technology.application.ITechnologyService;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.catalog.technology.contract.TechnologyRequest;
import com.arinno.canopus.reporting.application.ReportingService;

@ExtendWith(MockitoExtension.class)
class TechnologyControllerSecurityTests {

    @Mock
    private ITechnologyService technologyService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TechnologyMapper technologyMapper;

    @Mock
    private ReportingService reportingService;



    @Test
    void createRejectsResponsibleFromAnotherCompany() throws Exception {
        // Arrange (Preparación)
        Company company = new Company();
        company.setId(1L);
        TechnologyRequest request = new TechnologyRequest();
        request.setResponsibleId(2L);

        when(jwtService.getCompanyFromToken("Bearer token")).thenReturn(company);
        
        // Configura el mock para que simule el nuevo comportamiento del servicio:
        // Al no encontrar el usuario, lanza directamente un ResponseStatusException con NOT_FOUND (404)
        when(userService.findByIdAndCompany(2L, company))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no se encontró"));

        TechnologyController controller = new TechnologyController(
            technologyService, jwtService, userService, technologyMapper, reportingService);

        // Act & Assert (Acción y Verificación)
        assertThatThrownBy(() -> controller.create(request, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                // Verificamos que ahora el sistema responda con NOT_FOUND (404) en lugar de BAD_REQUEST
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

}
