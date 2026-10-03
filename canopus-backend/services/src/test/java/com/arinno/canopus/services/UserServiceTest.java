package com.arinno.canopus.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserProfileRequest;
import com.arinno.canopus.organization.technology.domain.Technology;
import com.arinno.canopus.organization.technology.domain.UserTechnology;
import com.arinno.canopus.organization.user.application.UserServiceImpl;
import com.arinno.canopus.error.EmailAlreadyExistsException;
import com.arinno.canopus.error.TechnologyNotFoundException;
import com.arinno.canopus.error.UsernameAlreadyExistsException;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.technology.infrastructure.persistence.TechnologyRepository;
import com.arinno.canopus.organization.user.infrastructure.persistence.UserRepository;
import com.arinno.canopus.organization.technology.infrastructure.persistence.UserTechnologyRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

        @Mock
        private TechnologyRepository technologyRepository;

        @Mock
        private UserTechnologyRepository userTechnologyRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Debería lanzar UsernameAlreadyExistsException cuando el username ya existe al dar de alta")
    void shouldThrowExceptionWhenUsernameAlreadyExistsOnCreate() {
        // Given (Dado un usuario nuevo y un username ya ocupado en el sistema)
        User newUser = new User();
        newUser.setUsername("prueba");
        newUser.setEmail("nuevo@arinno.com");
        newUser.setPassword("password123");

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setUsername("prueba");

        // Simulamos que el repositorio encuentra el username repetido
        when(userRepository.findByUsername("prueba")).thenReturn(Optional.of(existingUser));

        // When & Then (Comprobamos que se lance la excepción correcta de negocio con su texto)
        assertThatThrownBy(() -> userService.save(newUser))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("El nombre de usuario 'prueba' ya se encuentra registrado");

        // Verificación mecánica de seguridad: El servicio NUNCA debe ordenar un save al repositorio si falla el flujo
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar EmailAlreadyExistsException cuando el email ya existe al dar de alta")
    void shouldThrowExceptionWhenEmailAlreadyExistsOnCreate() {
        // Given (Dado un username libre pero un email idéntico al de otro compañero)
        User newUser = new User();
        newUser.setUsername("libre");
        newUser.setEmail("antonio.arino@servexternos.gruposantander.com");
        newUser.setPassword("password123");

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setEmail("antonio.arino@servexternos.gruposantander.com");

        when(userRepository.findByUsername("libre")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("antonio.arino@servexternos.gruposantander.com"))
                .thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.save(newUser))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ya se encuentra registrado en el sistema");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar EmailAlreadyExistsException al modificar el perfil si el email le pertenece a un tercero")
    void shouldThrowExceptionWhenEmailBelongsToOtherUserOnUpdateProfile() {
        // Given (Un usuario editando su perfil e inyectando un email que ya posee otro ID)
        Long miId = 1L;
        Long otroId = 2L;

        UserProfileRequest request = new UserProfileRequest();
        request.setEmail("antonio.arino@servexternos.gruposantander.com");
        request.setName("Prueba");
        request.setLastname("Modificado");

        User miUsuarioDb = new User();
        miUsuarioDb.setId(miId);
        miUsuarioDb.setEmail("mi-correo-anterior@arinno.com");

        User otroUsuarioDb = new User();
        otroUsuarioDb.setId(otroId); // ID diferente
        otroUsuarioDb.setEmail("antonio.arino@servexternos.gruposantander.com");

        when(userRepository.findById(miId)).thenReturn(Optional.of(miUsuarioDb));
        when(userRepository.findByEmail("antonio.arino@servexternos.gruposantander.com"))
                .thenReturn(Optional.of(otroUsuarioDb));

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(request, miId))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ya se encuentra registrado por otro usuario");

        verify(userRepository, never()).save(any(User.class));
    }

        @Test
        void updateProfileResolvesTechnologyIdsWithinUsersCompany() {
                Long userId = 1L;
                Company company = new Company();
                company.setId(4L);
                User user = new User();
                user.setId(userId);
                user.setCompany(company);

                Technology technology = new Technology();
                technology.setId(7L);
                technology.setCompany(company);

                UserProfileRequest request = new UserProfileRequest();
                request.setName("Name");
                request.setLastname("Lastname");
                request.setEmail("user@example.com");
                request.setTechnologies(List.of(technology.getId()));

                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
                when(technologyRepository.findByIdAndCompany(technology.getId(), company)).thenReturn(Optional.of(technology));

                userService.updateProfile(request, userId);

                verify(userTechnologyRepository).deleteByUser(user);
                verify(userTechnologyRepository).saveAll(argThat((Iterable<UserTechnology> associations) -> {
                        var iterator = associations.iterator();
                        if (!iterator.hasNext()) {
                                return false;
                        }
                        UserTechnology association = iterator.next();
                        return association.getUser() == user && association.getTechnology() == technology
                                        && association.getCompany() == company;
                }));
                verify(technologyRepository).findByIdAndCompany(technology.getId(), company);
                verify(userRepository).save(user);
        }

        @Test
        void updateProfileRejectsTechnologyOutsideUsersCompany() {
                Long userId = 1L;
                Company company = new Company();
                company.setId(4L);
                User user = new User();
                user.setId(userId);
                user.setCompany(company);

                UserProfileRequest request = new UserProfileRequest();
                request.setName("Name");
                request.setLastname("Lastname");
                request.setEmail("user@example.com");
                request.setTechnologies(List.of(7L));

                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
                when(technologyRepository.findByIdAndCompany(7L, company)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.updateProfile(request, userId))
                                .isInstanceOf(TechnologyNotFoundException.class)
                                .hasMessageContaining("Tecnología no encontrada para la empresa");
                verify(userRepository, never()).save(any(User.class));
        }
}
