package com.arinno.canopus.delivery.project.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.error.ProjectNotFoundException;
import com.arinno.canopus.delivery.project.infrastructure.persistence.ProjectRepository;

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

    // Modifica únicamente este método dentro de tu ProjectServiceImpl.java
    @Override
    @Transactional(readOnly = true)
    public Project findByIdAndCompany(Long id, Company company) {
        // CORRECCIÓN: Lanzamos ProjectNotFoundException en lugar de ResponseStatusException
        return projectRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ProjectNotFoundException("Proyecto no encontrado para la empresa con ID: " + id));
    }




    @Override
    @Transactional		
    public void deleteByIdAndCompany(Long id, Company company) {
        // Reutilizamos el método de arriba para validar su existencia de forma segura antes de lanzar el borrado a MySQL
        Project project = findByIdAndCompany(id, company);
        projectRepository.delete(project);
    }

	@Override
	@Transactional	
	public Project save(Project project) {
		return projectRepository.save(Objects.requireNonNull(project, "project must not be null"));
	}
	
	@Override
	@Transactional
	public List<Project> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
		return projectRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
	}

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
