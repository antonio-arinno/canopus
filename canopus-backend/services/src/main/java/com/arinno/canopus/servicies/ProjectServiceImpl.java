package com.arinno.canopus.servicies;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Status;
import com.arinno.canopus.repositories.ProjectRepository;

@Service
public class ProjectServiceImpl implements IProjectService {

    @Autowired
	private ProjectRepository projectRepository;


	@Override
	@Transactional(readOnly = true)
	public List<Project> findByCompany(Company company) {
		return (List<Project>) projectRepository.findByCompany(company);
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

}
