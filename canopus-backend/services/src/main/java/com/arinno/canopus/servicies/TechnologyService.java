package com.arinno.canopus.servicies;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
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
    public Technology findByIdAndCompany(Long id, Company company) {
        Technology technology = technologyRepository.findByIdAndCompany(id, company);
        return Objects.requireNonNullElse(technology, new Technology());
    }

    @Override
    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
        return technologyRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) {
//        technologyRepository.deleteRelationsByTechnologyId(id);
        technologyRepository.deleteByIdAndCompany(id, company);
    }

}
