package com.arinno.canopus.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;

public interface ProductRepository extends CrudRepository<Product, Long> {

    public List<Product> findByCompany(Company company);

    public List<Product> findByResponsible(User user);

    public List<Product> findByResponsibleAndCompany(User user, Company company);

    public List<Product> findByTechnology(Technology technology);

    public Product findByIdAndCompany(Long id, Company company);

    public void deleteByIdAndCompany(Long id, Company company);

    public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    @Query("select p from Product p left join Project pr on p.id = pr.product.id left join pr.contributors prc where (prc.id = ?1 or p.responsible.id = ?1) and p.company.id = ?2")
	public List<Product> findByContributorAndCompany(Long id, Long id2);

/*
    @Query("select p from Product p left join p.projects pr on p.id = pr.product.id left join pr.contributors prc where prc.id = ?1 and pr.status <> Status.PRODUCTION and p.company.id = ?2")
	public List<Product> findByNotProductionAndContributorAndCompany(Long id, Long company_id);

    @Query("select p from Product p left join p.projects pr on p.id = pr.product.id left join pr.contributors prc where prc.id = ?1 and pr.status <> Status.PRODUCTION")
	public List<Product> findByNotProductionAndContributor(Long id);
*/
    public Integer countByResponsible(User responsible);

    public Integer countByTechnology(Technology technology);

}
