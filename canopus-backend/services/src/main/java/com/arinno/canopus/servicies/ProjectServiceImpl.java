package com.arinno.canopus.servicies;

//import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
		return projectRepository.findByCompany(company);
	}
	
	@Override
	public List<Project> findByResponsible(User responsible) {
		return projectRepository.findByResponsible(responsible);
	}

	@Override
	public List<Project> findByResponsibleAndDateProIsNull(User responsible) {
		return projectRepository.findByResponsibleAndDateProIsNull(responsible);
	}

	@Override
	public List<Project> findByResponsibleAndCompany(User responsible, Company company) {
		return projectRepository.findByResponsibleAndCompany(responsible, company);
	}

	@Override
	public List<Project> findByResponsibleAndCompanyAndDateProIsNull(User responsible, Company company) {
		return projectRepository.findByResponsibleAndCompanyAndDateProIsNull(responsible, company);
	}

	@Override
	public List<Project> findByCompanyAndDateProIsNull(Company company) {
		return projectRepository.findByCompanyAndDateProIsNull(company);
	}

	@Override
	public Project findByIdAndCompany(Long id, Company company) {
		Project project = projectRepository.findByIdAndCompany(id, company);
		return Objects.requireNonNullElse(project, new Project());
	}

	@Override
	@Transactional	
	public Project save(Project project) {
		return projectRepository.save(Objects.requireNonNull(project, "project must not be null"));
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
		return projectRepository.findByProduct(product);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Project> findByProductAndCompany(Product product, Company company) {
		return projectRepository.findByProductAndCompany(product, company);
	}

	@Override
	public List<Project> findByContributorAndNotProductionAndCompany(Long id, Long companyId) {
		return projectRepository.findByContributorAndNotProductionAndCompany(id, companyId);
	}

	@Override
	public List<Project> findByContributorOpenDateAndCompany(Long id, LocalDate date, Long companyId) {
		return projectRepository.findByContributorOpenDateAndCompany(id, date, companyId);
	}

}
