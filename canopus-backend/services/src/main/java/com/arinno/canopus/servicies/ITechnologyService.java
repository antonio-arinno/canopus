package com.arinno.canopus.servicies;

import java.util.List;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Technology;

public interface ITechnologyService {

    public List<Technology> findByCompany(Company company);
    
    public Technology save(Technology technology);

    public Technology findByIdAndCompany(Long id, Company company);

    public List<Technology> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

    public void deleteByIdAndCompany(Long id, Company company);

}
