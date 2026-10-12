package com.arinno.canopus.catalog.technology.api;



import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.arinno.canopus.catalog.technology.api.mapper.TechnologyMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.technology.application.ITechnologyService;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.catalog.technology.contract.TechnologyRequest;
import com.arinno.canopus.catalog.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.reporting.application.ReportingService;
import com.arinno.canopus.reporting.contract.TechnologyMetrics;
import com.arinno.canopus.shared.contract.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/technology")
public class TechnologyController {

	private final ITechnologyService technologyService; 
	private final JwtService jwtService;
	private final UserService userService;
	private final TechnologyMapper technologyMapper;
	private final ReportingService reportingService;

    public TechnologyController(
			ITechnologyService technologyService, 
			JwtService jwtService, 
			UserService userService, 
			TechnologyMapper technologyMapper,
			ReportingService reportingService) {
		this.technologyService = technologyService;
		this.jwtService = jwtService;
		this.userService = userService;
		this.technologyMapper = technologyMapper;
		this.reportingService = reportingService;
	}

	@GetMapping("/page")
	public ResponseEntity<PageResponse<TechnologyResponse>> listPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestHeader(value = "Authorization") String auth) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));
		Page<Technology> technologies = technologyService.findPageByCompany(
				jwtService.getCompanyFromToken(auth), pageable);
		Map<Long, TechnologyMetrics> metricsByTechnology = reportingService.technologyMetricsByIds(
				technologies.getContent().stream().map(Technology::getId).toList());
		return ResponseEntity.ok(PageResponse.from(technologies,
				technology -> technologyMapper.toSummary(technology, metricsByTechnology.get(technology.getId()))));
	}

	@GetMapping
	public ResponseEntity<List<TechnologyResponse>> list(@RequestHeader(value="Authorization") String auth) {	
		List<Technology> technologies = technologyService.findByCompany(jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(toSummaries(technologies));
	}

	@PostMapping	
	public ResponseEntity<Void> create(@Valid @RequestBody TechnologyRequest request, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Technology technology = technologyMapper.toEntity(request);
		applyCompanyScopedResponsible(technology, request, company);
		technology.setCompany(company);
		technologyService.save(technology);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}	

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@Valid @RequestBody TechnologyRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		
		// El método findByIdAndCompany ya arroja TechnologyNotFoundException si no existe,
		// limpiando por completo el código de condicionales innecesarios en el controlador
		technologyService.findByIdAndCompany(id, company);
		
		Technology technology = technologyMapper.toEntity(request);
		technology.setId(id);
		applyCompanyScopedResponsible(technology, request, company);
		technology.setCompany(company);
		technologyService.save(technology);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TechnologyResponse> getTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Technology technology = technologyService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(technologyMapper.toDetail(technology, reportingService.technologyMetrics(technology)));
	}

	private List<TechnologyResponse> toSummaries(List<Technology> technologies) {
		Map<Long, TechnologyMetrics> metricsByTechnology = reportingService.technologyMetricsByIds(
				technologies.stream().map(Technology::getId).toList());
		return technologies.stream()
				.map(technology -> technologyMapper.toSummary(technology, metricsByTechnology.get(technology.getId())))
				.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		technologyService.deleteByIdAndCompany(id, company);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/select/{term}")
	public ResponseEntity<List<Technology>> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth) {	
		List<Technology> technologies = technologyService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
		return ResponseEntity.ok(technologies);
	}	

	private void applyCompanyScopedResponsible(Technology technology, TechnologyRequest request, Company company) {
		User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company);
		technology.setResponsible(responsible);
	}
}

