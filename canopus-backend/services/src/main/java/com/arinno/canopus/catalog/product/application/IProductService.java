package com.arinno.canopus.catalog.product.application;

import java.util.List;


import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.organization.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;

public interface IProductService {
  
    public List<Product> findByCompany(Company company);

    public List<Product> findByResponsibleAndCompany(User user, Company company);

    public List<Product> findByResponsibleOrBackupAndCompany(Long userId, Long companyId);

    public List<Product> findByTechnologyAndCompany(Technology technology, Company company);
    
    Product findByIdAndCompany(Long id, Company company);

    public void deleteByIdAndCompany(Long id, Company company);
    
    Product save(Product product);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public List<Product> findByContributorAndCompany(Long id, Long id2);

    public Integer countByResponsible(User responsible);

    public Integer countByTechnology(Technology technology);


    
}
