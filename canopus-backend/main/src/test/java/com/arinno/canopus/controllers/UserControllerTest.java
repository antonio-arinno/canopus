package com.arinno.canopus.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.organization.user.api.UserController;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.reporting.application.ReportingService;
import com.arinno.canopus.reporting.contract.UserMetrics;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ReportingService reportingService;

    @Test
    void meDelegatesResponseBuildingToUserMapper() {
        // Arrange (Preparación)
        Company company = new Company();
        company.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setCompany(company);

        UserResponse mappedResponse = UserResponse.builder()
                .id(10L)
                .username("testuser")
                .countProducts(3)
                .countProjects(5)
                .time(40)
                .build();

        when(jwtService.getUserFromToken("Bearer token")).thenReturn(user);
        when(jwtService.getCompanyFromToken("Bearer token")).thenReturn(company);
        
        // CORRECCIÓN 1: El servicio ahora devuelve el objeto User directamente, no un Optional
        when(userService.findByIdAndCompany(10L, company)).thenReturn(user);
        UserMetrics metrics = new UserMetrics(3, 5, 40);
        when(reportingService.userMetrics(user)).thenReturn(metrics);
        when(userService.findTechnologiesByUser(user)).thenReturn(java.util.List.<UserTechnologyResponse>of());
        when(userMapper.toDetail(user, metrics, java.util.List.of())).thenReturn(mappedResponse);

        UserController controller = new UserController(userService, jwtService, userMapper, reportingService);

        // Act (Acción)
        // CORRECCIÓN 2: Tipamos la respuesta con <UserResponse> en lugar de <?>
        ResponseEntity<UserResponse> response = controller.user("Bearer token");

        // Assert (Verificación)
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(mappedResponse);
    }

}

