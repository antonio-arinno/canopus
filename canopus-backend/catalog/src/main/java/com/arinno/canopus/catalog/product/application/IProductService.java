package com.arinno.canopus.catalog.product.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;

public interface IProductService {

    Page<Product> findPageByCompany(Company company, Pageable pageable);

    Page<Product> findPageByResponsibleAndCompany(User user, Company company, Pageable pageable);
  
    public List<Product> findByCompany(Company company);

    public List<Product> findByResponsibleAndCompany(User user, Company company);

    public List<Product> findByResponsibleOrBackupAndCompany(Long userId, Long companyId);

    public List<Product> findByTechnologyAndCompany(Technology technology, Company company);
    
    Product findByIdAndCompany(Long id, Company company);

    public void deleteByIdAndCompany(Long id, Company company);
    
    Product save(Product product);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    public Integer countByResponsible(User responsible);

    public Integer countByTechnology(Technology technology);


    
}
