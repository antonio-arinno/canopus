package com.arinno.canopus.catalog.technology.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.technology.domain.Technology;

public interface ITechnologyService {

    Page<Technology> findPageByCompany(Company company, Pageable pageable);

    public List<Technology> findByCompany(Company company);
    
    public Technology save(Technology technology);

    public Technology findByIdAndCompany(Long id, Company company);

    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

    public void deleteByIdAndCompany(Long id, Company company);

}
