package com.arinno.canopus.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.arinno.canopus.controllers.mapper.UserMapper;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Test
    void meDelegatesResponseBuildingToUserMapper() {
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
        when(userService.findByIdAndCompany(10L, company)).thenReturn(Optional.of(user));
        when(userMapper.toDetail(user)).thenReturn(mappedResponse);

        UserController controller = new UserController(userService, jwtService, userMapper);

        ResponseEntity<?> response = controller.user("Bearer token");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(mappedResponse);
    }
}

