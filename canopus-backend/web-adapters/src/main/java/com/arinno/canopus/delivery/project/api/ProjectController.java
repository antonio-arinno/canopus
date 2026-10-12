package com.arinno.canopus.delivery.project.api;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.delivery.project.api.mapper.ProjectMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.delivery.project.application.IProjectService;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.delivery.project.contract.ProjectRequest;
import com.arinno.canopus.delivery.project.contract.ProjectResponse;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.reporting.application.ReportingService;
import com.arinno.canopus.reporting.contract.ProjectMetrics;
import com.arinno.canopus.shared.contract.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final IProjectService projectService;
	private final JwtService jwtService;
	private final IProductService productService;
	private final UserService userService;
	private final ProjectMapper projectMapper;
	private final ReportingService reportingService;

	ProjectController(
			IProjectService projectService, 
			JwtService jwtService, 
			IProductService productService, 
			UserService userService, 
			ProjectMapper projectMapper,
			ReportingService reportingService) {
		this.projectService = projectService;
		this.jwtService = jwtService;
		this.productService = productService;
		this.userService = userService;
		this.projectMapper = projectMapper;
		this.reportingService = reportingService;
	}

	@GetMapping("/page")
	public ResponseEntity<PageResponse<ProjectResponse>> listPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "personal") String scope,
			@RequestParam(defaultValue = "opened") String state,
			@RequestHeader(value = "Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));
		boolean personal = !"global".equalsIgnoreCase(scope);
		boolean opened = "opened".equalsIgnoreCase(state);
		Page<Project> projects;
		if (personal && opened) {
			projects = projectService.findPageByResponsibleAndCompanyAndDateProIsNull(
					jwtService.getUserFromToken(auth), company, pageable);
		} else if (personal) {
			projects = projectService.findPageByResponsibleAndCompany(
					jwtService.getUserFromToken(auth), company, pageable);
		} else if (opened) {
			projects = projectService.findPageByCompanyAndDateProIsNull(company, pageable);
		} else {
			projects = projectService.findPageByCompany(company, pageable);
		}

		Map<Long, ProjectMetrics> metricsByProject = reportingService.projectMetricsByIds(
				projects.getContent().stream().map(Project::getId).toList());
		return ResponseEntity.ok(PageResponse.from(projects,
				project -> projectMapper.toSummary(project, metricsByProject.get(project.getId()))));
	}

	@GetMapping	
	public ResponseEntity<List<ProjectResponse>> listPersonalOpened(@RequestHeader(value="Authorization") String auth) {
		List<Project> projects = projectService.findByResponsibleAndCompanyAndDateProIsNull(
				jwtService.getUserFromToken(auth), 
				jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(projects));
	}

	@GetMapping("/all")
	public ResponseEntity<List<ProjectResponse>> listPersonalAll(@RequestHeader(value="Authorization") String auth) {
		List<Project> projects = projectService.findByResponsibleAndCompany(
				jwtService.getUserFromToken(auth), 
				jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(projects));
	}

	@GetMapping("/global")
	public ResponseEntity<List<ProjectResponse>> listGlobalOpened(@RequestHeader(value="Authorization") String auth) {
		List<Project> projects = projectService.findByCompanyAndDateProIsNull(jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(projects));
	}

	@GetMapping("/globalall")
	public ResponseEntity<List<ProjectResponse>> listGlobalAll(@RequestHeader(value="Authorization") String auth) {
		List<Project> projects = projectService.findByCompany(jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(projects));
	}

	@GetMapping("/product/{id}")
	public ResponseEntity<List<ProjectResponse>> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = new Product();
		product.setId(id);
		List<Project> projects = projectService.findByProductAndCompany(
				product, 
				jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(projects));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponse> project(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		// El servicio garantiza el aislamiento. Si no existe, corta con tu excepcion de negocio
		Project project = projectService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(projectMapper.toDetail(project, reportingService.projectMetrics(project)));
	}

	@PostMapping
	public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Project project = projectMapper.toEntity(request);
		
		applyCompanyScopedRelations(project, request, company);
		project.setCompany(company);
		Project savedProject = projectService.save(project);
		
		// MEJORA: Devolvemos el DTO mapeado en vez de la entidad cruda relacional
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(projectMapper.toDetail(savedProject, reportingService.projectMetrics(savedProject)));
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
		List<Project> projects = projectService.findByContributorOpenDateAndCompany(
				jwtService.getUserFromToken(auth).getId(), 
				date, 
				jwtService.getCompanyFromToken(auth).getId());
		return ResponseEntity.ok(toSummaries(projects));
	}

	private List<ProjectResponse> toSummaries(List<Project> projects) {
		Map<Long, ProjectMetrics> metricsByProject = reportingService.projectMetricsByIds(
				projects.stream().map(Project::getId).toList());
		return projects.stream()
				.map(project -> projectMapper.toSummary(project, metricsByProject.get(project.getId())))
				.toList();
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

