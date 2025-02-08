package com.arinno.canopus.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.User;

public interface ProductRepository extends CrudRepository<Product, Long> {

    public List<Product> findByCompany(Company company);

    public Product findByIdAndCompany(Long id, Company company);

    public void deleteByIdAndCompany(Long id, Company company);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    @Query("select p from Product p left join p.projects pr on p.id = pr.product.id left join pr.contributors prc where prc.id = ?1 and pr.status <> Status.PRODUCTION and p.company.id = ?2")
	public List<Product> findByNotProductionAndContributorAndCompany(Long id, Long company_id);

    @Query("select p from Product p left join p.projects pr on p.id = pr.product.id left join pr.contributors prc where prc.id = ?1 and pr.status <> Status.PRODUCTION")
	public List<Product> findByNotProductionAndContributor(Long id);

    public Integer countByResponsible(User responsible);

}
