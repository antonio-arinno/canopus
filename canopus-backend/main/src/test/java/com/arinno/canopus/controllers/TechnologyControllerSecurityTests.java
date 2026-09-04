package com.arinno.canopus.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.TechnologyMapper;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.TechnologyRequest;
import com.arinno.canopus.servicies.ITechnologyService;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

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

    @Test
    void createRejectsResponsibleFromAnotherCompany() throws Exception {
        Company company = new Company();
        company.setId(1L);
        TechnologyRequest request = new TechnologyRequest();
        request.setResponsibleId(2L);

        when(jwtService.getCompanyFromToken("Bearer token")).thenReturn(company);
        when(userService.findByIdAndCompany(2L, company)).thenReturn(Optional.empty());

        TechnologyController controller = new TechnologyController(technologyService, jwtService, userService, technologyMapper);

        assertThatThrownBy(() -> controller.save(request, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
