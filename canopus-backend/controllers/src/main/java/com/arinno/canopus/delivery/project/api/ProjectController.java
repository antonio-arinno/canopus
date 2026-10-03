package com.arinno.canopus.delivery.project.api;


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

import com.arinno.canopus.delivery.project.api.mapper.ProjectMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.delivery.project.application.IProjectService;
import com.arinno.canopus.services.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.delivery.project.contract.ProjectRequest;
import com.arinno.canopus.delivery.project.contract.ProjectResponse;
import com.arinno.canopus.organization.user.domain.User;

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
		// El servicio garantiza el aislamiento. Si no existe, corta con tu excepcion de negocio
		Project project = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(projectMapper.toDetail(project));
	}

	@PostMapping
	public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Project project = projectMapper.toEntity(request);
		
		applyCompanyScopedRelations(project, request, company);
		project.setCompany(company);
		Project savedProject = projectService.save(project);
		
		// MEJORA: Devolvemos el DTO mapeado en vez de la entidad cruda relacional
		return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toDetail(savedProject));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Void> edit(@Valid @RequestBody ProjectRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		
		// MEJORA: El método findByIdAndCompany ya arroja ProjectNotFoundException si no existe,
		// limpiando por completo el código de condicionales innecesarios en el controlador
		projectService.findByIdAndCompany(id, company);
		
		Project project = projectMapper.toEntity(request);
		project.setId(id);
		applyCompanyScopedRelations(project, request, company);
		project.setCompany(company);
		projectService.save(project);
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		projectService.deleteByIdAndCompany(id, company);
		return ResponseEntity.noContent().build();
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

