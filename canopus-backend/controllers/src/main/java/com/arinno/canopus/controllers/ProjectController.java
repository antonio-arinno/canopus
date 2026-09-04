package com.arinno.canopus.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.ProjectMapper;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.ProjectRequest;
import com.arinno.canopus.entities.ProjectResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.ProjectDataIntegrityException;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final IProjectService projectService;

	private final JwtService jwtService;

	private final IProductService productService;

	private final UserService userService;

	private final ProjectMapper projectMapper;

	ProjectController(IProjectService projectService, JwtService jwtService, IProductService productService, UserService userService, ProjectMapper projectMapper) {
		this.projectService = projectService;
		this.jwtService = jwtService;
		this.productService = productService;
		this.userService = userService;
		this.projectMapper = projectMapper;
	}

	@GetMapping	
	public List<ProjectResponse> listPersonalOpened(@RequestHeader(value="Authorization") String auth){
		return projectService.findByResponsibleAndCompanyAndDateProIsNull(jwtService.getUserFromToken(auth), jwtService.getCompanyFromToken(auth)).stream().map(projectMapper::toSummary).toList();
	}

	@GetMapping("/all")
	public List<ProjectResponse> listPersonalAll(@RequestHeader(value="Authorization") String auth){
		return projectService.findByResponsibleAndCompany(jwtService.getUserFromToken(auth), jwtService.getCompanyFromToken(auth)).stream().map(projectMapper::toSummary).toList();
	}

	@GetMapping("/global")
	public List<ProjectResponse> listGlobalOpened(@RequestHeader(value="Authorization") String auth){
		return projectService.findByCompanyAndDateProIsNull(jwtService.getCompanyFromToken(auth)).stream().map(projectMapper::toSummary).toList();
	}

	@GetMapping("/globalall")
	public List<ProjectResponse> listGlobalAll(@RequestHeader(value="Authorization") String auth){
		return projectService.findByCompany(jwtService.getCompanyFromToken(auth)).stream().map(projectMapper::toSummary).toList();
	}

	@GetMapping("/product/{id}")
	public List<ProjectResponse> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = new Product();
		product.setId(id);
		return projectService.findByProductAndCompany(product, jwtService.getCompanyFromToken(auth)).stream().map(projectMapper::toSummary).toList();
	}

	@GetMapping("/{id}")
	public ProjectResponse project(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Project project = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		if (project == null || project.getId() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado para la empresa.");
		}
		return projectMapper.toDetail(project);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Project create(@Valid @RequestBody ProjectRequest request, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Company company = jwtService.getCompanyFromToken(auth);
			Project project = projectMapper.toEntity(request);
			applyCompanyScopedRelations(project, request, company);
			project.setCompany(company);
			return projectService.save(project);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
	
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public void edit(@Valid @RequestBody ProjectRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Project projectDb = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
			if (projectDb == null || projectDb.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado para la empresa.");
			}
			Company company = jwtService.getCompanyFromToken(auth);
			Project project = projectMapper.toEntity(request);
			project.setId(id);
			applyCompanyScopedRelations(project, request, company);
			project.setCompany(company);
			projectService.save(project);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Project project = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
			if (project == null || project.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado para la empresa.");
			}
			projectService.deleteByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ProjectDataIntegrityException();
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}	

	private void applyCompanyScopedRelations(Project project, ProjectRequest request, Company company) {
		Product product = productService.findByIdAndCompany(request.getProductId(), company);
		User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company).orElse(null);
		if (product.getId() == null || responsible == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto o responsable no válido para la empresa.");
		}

		List<User> contributors = new ArrayList<>();
		for (Long contributorId : request.getContributorIds()) {
				User companyContributor = userService.findByIdAndCompany(contributorId, company).orElse(null);
				if (companyContributor == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Colaborador no válido para la empresa.");
				}
				contributors.add(companyContributor);
		}

		project.setProduct(product);
		project.setResponsible(responsible);
		project.setContributors(contributors);
	}
	
	@GetMapping("/select/{term}")
	public List<Project> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return projectService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
	}		
	
	@GetMapping("/contributor")
	public List<Project> listNotProductionContributor(@RequestHeader(value="Authorization") String auth){
		return projectService.findByContributorAndNotProductionAndCompany(jwtService.getUserFromToken(auth).getId(), jwtService.getCompanyFromToken(auth).getId());
	}	

	@GetMapping("/contributor/{date}")
	public List<ProjectResponse> listOpenedContributorDate(
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestHeader(value="Authorization") String auth) {
    
	return projectService.findByContributorOpenDateAndCompany(jwtService.getUserFromToken(auth).getId(), date, jwtService.getCompanyFromToken(auth).getId())
		.stream()
		.map(projectMapper::toSummary)
		.toList();
	}
}
