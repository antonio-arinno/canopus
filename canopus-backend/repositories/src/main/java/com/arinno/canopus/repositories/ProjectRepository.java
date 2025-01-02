package com.arinno.canopus.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Status;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    public List<Project> findByCompany(Company company);
	
	public List<Project> findByStatus(Status status);
	
	@Query("select p from Project p where p.status <> ?1")	
	public List<Project> findByStatusNotProduction(Status status);
	
	public Project findByIdAndCompany(Long id, Company company);
	
	public void deleteByIdAndCompany(Long id, Company company);
	
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company);	

}
