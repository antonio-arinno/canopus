package com.arinno.canopus.servicies;

import java.util.List;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.User;

public interface IProductService {
  
    public List<Product> findByCompany(Company company);
    
    Product findByIdAndCompany(Long id, Company company);
    
    public void deleteByIdAndCompany(Long id, Company company);
    
    Product save(Product product);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public List<Product> findByProjectNotProductionAndContributorAndCompany(Long id, Long company_id);

    public List<Product> findByProjectNotProductionAndContributor(Long id);

    public Integer countByResponsible(User responsible);
}
