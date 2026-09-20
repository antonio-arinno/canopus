package com.arinno.canopus.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.ProjectMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.ProjectRequest;
import com.arinno.canopus.entities.ProjectResponse;
import com.arinno.canopus.entities.User;
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

	ProjectController(
			IProjectService projectService, 
			JwtService jwtService, 
			IProductService productService, 
			UserService userService, 
			ProjectMapper projectMapper) {
		this.projectService = projectService;
		this.jwtService = jwtService;
		this.productService = productService;
		this.userService = userService;
		this.projectMapper = projectMapper;
	}

	@GetMapping	
	public ResponseEntity<List<ProjectResponse>> listPersonalOpened(@RequestHeader(value="Authorization") String auth) {
		List<ProjectResponse> projects = projectService.findByResponsibleAndCompanyAndDateProIsNull(
				jwtService.getUserFromToken(auth), 
				jwtService.getCompanyFromToken(auth))
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

	@GetMapping("/all")
	public ResponseEntity<List<ProjectResponse>> listPersonalAll(@RequestHeader(value="Authorization") String auth) {
		List<ProjectResponse> projects = projectService.findByResponsibleAndCompany(
				jwtService.getUserFromToken(auth), 
				jwtService.getCompanyFromToken(auth))
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

	@GetMapping("/global")
	public ResponseEntity<List<ProjectResponse>> listGlobalOpened(@RequestHeader(value="Authorization") String auth) {
		List<ProjectResponse> projects = projectService.findByCompanyAndDateProIsNull(
				jwtService.getCompanyFromToken(auth))
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

	@GetMapping("/globalall")
	public ResponseEntity<List<ProjectResponse>> listGlobalAll(@RequestHeader(value="Authorization") String auth) {
		List<ProjectResponse> projects = projectService.findByCompany(
				jwtService.getCompanyFromToken(auth))
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

	@GetMapping("/product/{id}")
	public ResponseEntity<List<ProjectResponse>> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = new Product();
		product.setId(id);
		List<ProjectResponse> projects = projectService.findByProductAndCompany(
				product, 
				jwtService.getCompanyFromToken(auth))
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> project(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        // El servicio ya asegura el aislamiento por empresa o lanza 404
        Project project = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(projectMapper.toDetail(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        // El servicio se encarga de validar la existencia de forma atómica antes de borrar
        projectService.deleteByIdAndCompany(id, company);
        return ResponseEntity.noContent().build();
    }

	
	@PostMapping
	public ResponseEntity<Project> create(@Valid @RequestBody ProjectRequest request, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Project project = projectMapper.toEntity(request);
		applyCompanyScopedRelations(project, request, company);
		project.setCompany(company);
		Project savedProject = projectService.save(project);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Void> edit(@Valid @RequestBody ProjectRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
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
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	


	@GetMapping("/select/{term}")
	public ResponseEntity<List<Project>> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth) {	
		List<Project> projects = projectService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(projects);
	}		
	
	@GetMapping("/contributor")
	public ResponseEntity<List<Project>> listNotProductionContributor(@RequestHeader(value="Authorization") String auth) {
		List<Project> projects = projectService.findByContributorAndNotProductionAndCompany(
				jwtService.getUserFromToken(auth).getId(), 
				jwtService.getCompanyFromToken(auth).getId());
		return ResponseEntity.ok(projects);
	}	

	@GetMapping("/contributor/{date}")
	public ResponseEntity<List<ProjectResponse>> listOpenedContributorDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, 
			@RequestHeader(value="Authorization") String auth) {
		List<ProjectResponse> projects = projectService.findByContributorOpenDateAndCompany(
				jwtService.getUserFromToken(auth).getId(), 
				date, 
				jwtService.getCompanyFromToken(auth).getId())
				.stream()
				.map(projectMapper::toSummary)
				.toList();
		return ResponseEntity.ok(projects);
	}

	private void applyCompanyScopedRelations(Project project, ProjectRequest request, Company company) {
		Product product = productService.findByIdAndCompany(request.getProductId(), company);
		User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company);

		List<User> contributors = request.getContributorIds().stream()
				.map(contributorId -> userService.findByIdAndCompany(contributorId, company))
				.toList();

		project.setProduct(product);
		project.setResponsible(responsible);
		project.setContributors(contributors);
	}
}

