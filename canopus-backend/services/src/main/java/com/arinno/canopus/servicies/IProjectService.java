package com.arinno.canopus.servicies;

import java.sql.Date;
import java.util.List;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.User;

public interface IProjectService {

    public List<Project> findByCompany(Company company);

	public List<Project> findByResponsible(User responsible);

	public List<Project> findByResponsibleAndDateProIsNull(User responsible);

	public List<Project> findByCompanyAndDateProIsNull(Company company);

	public List<Project> findByProduct(Product product);
	
//	public List<Project> findByStatus(Status status);
	
//	public List<Project> findByStatusNotProduction();
	
	public Project findByIdAndCompany(Long id, Company company);
	
	public Project save(Project project);
	
	public void deleteByIdAndCompany(Long id, Company company);
	
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

	public List<Project> findByContributorAndNotProduction(Long id);

	public Integer countByResponsible(User responsible);

    public Integer countByProduct(Product product);

	public Integer countByTechnology(Long id);

    public List<Project> findByContributorOpenDate(Long id, Date date);

    public Integer countContributorsByProduct(Long id);

	public Integer countContributorsByTechnology(Long id);

}
