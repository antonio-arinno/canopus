package com.arinno.canopus.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.User;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    public List<Project> findByCompany(Company company);

	public List<Project> findByResponsibleAndCompany(User responsible, Company company);

	public List<Project> findByResponsibleAndCompanyAndDateProIsNull(User responsible, Company company);

	public List<Project> findByCompanyAndDateProIsNull(Company company);

	public List<Project> findByProductAndCompany(Product product, Company company);

	Optional<Project> findByIdAndCompany(Long id, Company company);
	
	public void deleteByIdAndCompany(Long id, Company company);
	
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

	@Query("select p from Project p left join p.contributors prc where prc.id = ?1 and p.datePro is null and p.company.id = ?2")
	public List<Project> findByContributorAndNotProductionAndCompany(Long id, Long companyId);

	public Integer countByResponsible(User responsible);

    public Integer countByProduct(Product product);

	@Query("select count(project) from Technology technology left join Product product on technology.id = product.technology.id left join Project project on product.id = project.product.id where technology.id = ?1")
	public Integer countByTechnology(Long id);

	@Query("select p from Project p left join p.contributors prc where prc.id = ?1 and p.dateDev <= ?2 and (p.datePro >= ?2 or p.datePro is null) and p.company.id = ?3")
	public List<Project> findByContributorOpenDateAndCompany(Long id, LocalDate date, Long companyId);


	@Query
	("select contributors from Product product left join Project project on product.id = project.product.id where product.id = ?1")
    public List<User> countContributorsByProduct(Long id);	

	@Query
	("select contributors from Technology technology left join Product product on technology.id = product.technology.id left join Project project on product.id = project.product.id where technology.id = ?1")
    public List<User> countContributorsByTechnology(Long id);	

}
