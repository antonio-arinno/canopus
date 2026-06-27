package com.arinno.canopus.servicies;

//import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.repositories.ProjectRepository;

@Service
public class ProjectServiceImpl implements IProjectService {
	
	private final ProjectRepository projectRepository;

	ProjectServiceImpl(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}
		
	@Override
	@Transactional(readOnly = true)
	public List<Project> findByCompany(Company company) {
		return (List<Project>) projectRepository.findByCompany(company);
	}
	
	@Override
	public List<Project> findByResponsible(User responsible) {
		return (List<Project>) projectRepository.findByResponsible(responsible);
	}

	@Override
	public List<Project> findByResponsibleAndDateProIsNull(User responsible) {
		return (List<Project>) projectRepository.findByResponsibleAndDateProIsNull(responsible);
	}

	@Override
	public List<Project> findByCompanyAndDateProIsNull(Company company) {
		return (List<Project>) projectRepository.findByCompanyAndDateProIsNull(company);
	}

	@Override
	public Project findByIdAndCompany(Long id, Company company) {
		return projectRepository.findByIdAndCompany(id, company);
	}

	@Override
	@Transactional	
	public Project save(Project project) {
		return projectRepository.save(project);
	}

	@Override
	@Transactional		
	public void deleteByIdAndCompany(Long id, Company company) {
		projectRepository.deleteByIdAndCompany(id, company);
	}
	
	@Override
	@Transactional
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
		return projectRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
	}
/*
	@Override
	@Transactional
	public List<Project> findByStatus(Status status) {
		return projectRepository.findByStatus(status);
	}

	@Override
	@Transactional
	public List<Project> findByStatusNotProduction() {
		return projectRepository.findByStatusNotProduction(Status.PRODUCTION);
	}
*/
	@Override
	@Transactional
	public Integer countByResponsible(User responsible) {
		return projectRepository.countByResponsible(responsible);
	}

	@Override
	@Transactional
	public Integer countByProduct(Product product) {
		return projectRepository.countByProduct(product);
	}

	@Override
	public List<Project> findByContributorAndNotProduction(Long id) {
		return projectRepository.findByContributorAndNotProduction(id);
	}
 
	@Override
	public List<Project> findByContributorOpenDate(Long id, LocalDate date) {
		return projectRepository.findByContributorOpenDate(id, date);
	}

	@Override
	public Integer countByTechnology(Long id) {
		return projectRepository.countByTechnology(id);
	}

	@Override
	public Integer countContributorsByProduct(Long id) {
		return projectRepository.countContributorsByProduct(id).size();
	}

	@Override
	public Integer countContributorsByTechnology(Long id) {
		return projectRepository.countContributorsByTechnology(id).size();
	}

	@Override
	@Transactional
	public List<Project> findByProduct(Product product) {
		return (List<Project>) projectRepository.findByProduct(product);
	}

}
