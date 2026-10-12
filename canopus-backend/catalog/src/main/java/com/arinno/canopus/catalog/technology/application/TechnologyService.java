package com.arinno.canopus.catalog.technology.application;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.catalog.technology.infrastructure.persistence.TechnologyRepository;

@Service
public class TechnologyService implements ITechnologyService {

    private final TechnologyRepository technologyRepository;

    TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Technology> findPageByCompany(Company company, Pageable pageable) {
        return technologyRepository.findByCompany(company, pageable);
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
                .orElseThrow(() -> new TechnologyNotFoundException("Tecnología no encontrada para la empresa con ID: " + id));
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) {
        Technology technology = findByIdAndCompany(id, company);
        technologyRepository.delete(technology);
    }

}
