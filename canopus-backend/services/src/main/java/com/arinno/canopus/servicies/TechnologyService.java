package com.arinno.canopus.servicies;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.repositories.TechnologyRepository;

@Service
public class TechnologyService implements ITechnologyService {

    @Autowired
    private TechnologyRepository technologyRepository;

    @Override
    public List<Technology> findByCompany(Company company) {
        return technologyRepository.findByCompany(company);
    }

    @Override
    public Technology save(Technology technology) {
        return technologyRepository.save(technology);
    }

    @Override
    public Technology findByIdAndCompany(Long id, Company company) {
        return technologyRepository.findByIdAndCompany(id, company);
    }

    @Override
    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
        return technologyRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) {
        technologyRepository.deleteByIdAndCompany(id, company);
    }

}
