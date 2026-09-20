package com.arinno.canopus.servicies;

import java.time.LocalDate;
import java.util.List;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.User;

public interface IProjectService {

    public List<Project> findByCompany(Company company);

	public List<Project> findByResponsibleAndCompany(User responsible, Company company);

	public List<Project> findByResponsibleAndCompanyAndDateProIsNull(User responsible, Company company);

	public List<Project> findByCompanyAndDateProIsNull(Company company);

	public List<Project> findByProductAndCompany(Product product, Company company);

	Project findByIdAndCompany(Long id, Company company);
	
	public Project save(Project project);
	
	public void deleteByIdAndCompany(Long id, Company company);
	
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

	public List<Project> findByContributorAndNotProductionAndCompany(Long id, Long companyId);

	public Integer countByResponsible(User responsible);

    public Integer countByProduct(Product product);

	public Integer countByTechnology(Long id);

    public Integer countContributorsByProduct(Long id);

	public Integer countContributorsByTechnology(Long id);

	List<Project> findByContributorOpenDateAndCompany(Long id, LocalDate date, Long companyId);

}
