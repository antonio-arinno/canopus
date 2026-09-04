package com.arinno.canopus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.servicies.CurrentUserContext;
import com.arinno.canopus.servicies.CurrentUserContextImpl;
import com.arinno.canopus.servicies.UserService;

@ExtendWith(MockitoExtension.class)
class CurrentUserContextTests {

    @Mock
    private UserService userService;

    private CurrentUserContext currentUserContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        currentUserContext = new CurrentUserContextImpl(userService);
    }

    @Test
    void resolvesCurrentUserAndCompanyFromSecurityContext() {
        Company company = new Company();
        company.setId(7L);
        company.setName("Acme");

        User user = new User();
        user.setId(11L);
        user.setUsername("ana");
        user.setCompany(company);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana", null, List.of()));

        when(userService.findByUsername("ana")).thenReturn(Optional.of(user));

        assertThat(currentUserContext.getCurrentUser().getUsername()).isEqualTo("ana");
        assertThat(currentUserContext.getCurrentCompany()).isSameAs(company);
    }
}
