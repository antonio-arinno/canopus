package com.arinno.canopus.servicies;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.repositories.TechnologyRepository;

@Service
public class TechnologyService implements ITechnologyService {

    private final TechnologyRepository technologyRepository;

    TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public List<Technology> findByCompany(Company company) {
        return technologyRepository.findByCompany(company);
    }

    @Override
    public Technology save(Technology technology) {
        return technologyRepository.save(Objects.requireNonNull(technology, "technology must not be null"));
    }

    @Override
    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
        return technologyRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
    }

    @Override
    @Transactional(readOnly = true)
    public Technology findByIdAndCompany(Long id, Company company) {
        return technologyRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tecnología no encontrada para la empresa."));
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) {
        Technology technology = findByIdAndCompany(id, company);
        technologyRepository.delete(technology);
    }

}
