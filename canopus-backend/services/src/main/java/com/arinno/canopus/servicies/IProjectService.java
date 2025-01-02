package com.arinno.canopus.servicies;

import java.util.List;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Status;

public interface IProjectService {

    public List<Project> findByCompany(Company company);
	
	public List<Project> findByStatus(Status status);
	
	public List<Project> findByStatusNotProduction();
	
	public Project findByIdAndCompany(Long id, Company company);
	
	public Project save(Project project);
	
	public void deleteByIdAndCompany(Long id, Company company);
	
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

}
