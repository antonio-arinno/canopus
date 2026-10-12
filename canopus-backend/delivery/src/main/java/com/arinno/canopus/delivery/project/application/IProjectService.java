package com.arinno.canopus.delivery.project.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.organization.user.domain.User;

public interface IProjectService {

	Page<Project> findPageByCompany(Company company, Pageable pageable);

	Page<Project> findPageByResponsibleAndCompany(User responsible, Company company, Pageable pageable);

	Page<Project> findPageByResponsibleAndCompanyAndDateProIsNull(User responsible, Company company, Pageable pageable);

	Page<Project> findPageByCompanyAndDateProIsNull(Company company, Pageable pageable);

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
