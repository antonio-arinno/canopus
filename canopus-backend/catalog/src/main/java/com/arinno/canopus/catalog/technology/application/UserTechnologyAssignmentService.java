package com.arinno.canopus.catalog.technology.application;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.catalog.technology.domain.UserTechnology;
import com.arinno.canopus.catalog.technology.infrastructure.persistence.TechnologyRepository;
import com.arinno.canopus.catalog.technology.infrastructure.persistence.UserTechnologyRepository;
import com.arinno.canopus.organization.user.application.UserTechnologyAssignments;
import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.organization.user.domain.User;

@Service
public class UserTechnologyAssignmentService implements UserTechnologyAssignments {

    private final TechnologyRepository technologyRepository;
    private final UserTechnologyRepository userTechnologyRepository;

    public UserTechnologyAssignmentService(TechnologyRepository technologyRepository,
            UserTechnologyRepository userTechnologyRepository) {
        this.technologyRepository = technologyRepository;
        this.userTechnologyRepository = userTechnologyRepository;
    }

    @Override
    @Transactional
    public void replace(User user, List<Long> technologyIds) {
        userTechnologyRepository.deleteByUser(user);
        if (technologyIds == null) {
            return;
        }

        List<UserTechnology> associations = new ArrayList<>();
        for (Long technologyId : new LinkedHashSet<>(technologyIds)) {
            if (technologyId == null) {
                throw new TechnologyNotFoundException("Tecnología no encontrada para la empresa.");
            }
            Technology technology = technologyRepository.findByIdAndCompany(technologyId, user.getCompany())
                    .orElseThrow(() -> new TechnologyNotFoundException(
                            "Tecnología no encontrada para la empresa: " + technologyId));

            UserTechnology association = new UserTechnology();
            association.setUser(user);
            association.setTechnology(technology);
            association.setCompany(user.getCompany());
            associations.add(association);
        }
        userTechnologyRepository.saveAll(associations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTechnologyResponse> findByUser(User user) {
        return userTechnologyRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<UserTechnologyResponse>> findByUsers(List<User> users) {
        if (users.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<UserTechnologyResponse>> technologiesByUser = new HashMap<>();
        for (UserTechnology association : userTechnologyRepository.findByUserIn(users)) {
            technologiesByUser.computeIfAbsent(association.getUser().getId(), ignored -> new ArrayList<>())
                    .add(toResponse(association));
        }
        return technologiesByUser;
    }

    private UserTechnologyResponse toResponse(UserTechnology association) {
        return UserTechnologyResponse.builder()
                .id(association.getTechnology().getId())
                .name(association.getTechnology().getName())
                .build();
    }
}