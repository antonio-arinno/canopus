package com.arinno.canopus.catalog.technology.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.catalog.technology.domain.UserTechnology;
import com.arinno.canopus.catalog.technology.infrastructure.persistence.TechnologyRepository;
import com.arinno.canopus.catalog.technology.infrastructure.persistence.UserTechnologyRepository;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;

@ExtendWith(MockitoExtension.class)
class UserTechnologyAssignmentServiceTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private UserTechnologyRepository userTechnologyRepository;

    @InjectMocks
    private UserTechnologyAssignmentService assignmentService;

    @Test
    void replaceResolvesTechnologyWithinUsersCompanyAndDeduplicatesIds() {
        Company company = company(4L);
        User user = new User();
        user.setCompany(company);
        Technology technology = new Technology();
        technology.setId(7L);
        technology.setCompany(company);
        when(technologyRepository.findByIdAndCompany(7L, company)).thenReturn(Optional.of(technology));

        assignmentService.replace(user, List.of(7L, 7L));

        verify(userTechnologyRepository).deleteByUser(user);
        verify(technologyRepository).findByIdAndCompany(7L, company);
        verify(userTechnologyRepository).saveAll(argThat((Iterable<UserTechnology> associations) -> {
            var iterator = associations.iterator();
            if (!iterator.hasNext()) {
                return false;
            }
            UserTechnology association = iterator.next();
            return !iterator.hasNext() && association.getUser() == user
                    && association.getTechnology() == technology && association.getCompany() == company;
        }));
    }

    @Test
    void replaceRejectsTechnologyOutsideUsersCompany() {
        Company company = company(4L);
        User user = new User();
        user.setCompany(company);
        when(technologyRepository.findByIdAndCompany(7L, company)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.replace(user, List.of(7L)))
                .isInstanceOf(TechnologyNotFoundException.class)
                .hasMessageContaining("Tecnología no encontrada para la empresa");

        verify(userTechnologyRepository, never()).saveAll(argThat(ignored -> true));
    }

    private Company company(Long id) {
        Company company = new Company();
        company.setId(id);
        return company;
    }
}