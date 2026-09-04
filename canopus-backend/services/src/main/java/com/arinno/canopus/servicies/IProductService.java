package com.arinno.canopus.servicies;

import java.util.List;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;

public interface IProductService {
  
    public List<Product> findByCompany(Company company);

    public List<Product> findByResponsibleAndCompany(User user, Company company);

    public List<Product> findByResponsibleOrBackupAndCompany(Long userId, Long companyId);

    public List<Product> findByTechnologyAndCompany(Technology technology, Company company);
    
    Product findByIdAndCompany(Long id, Company company);
    
    public void deleteByIdAndCompany(Long id, Company company) throws CustomException;
    
    Product save(Product product);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public List<Product> findByContributorAndCompany(Long id, Long id2);

    public Integer countByResponsible(User responsible);

    public Integer countByTechnology(Technology technology);


    
}
