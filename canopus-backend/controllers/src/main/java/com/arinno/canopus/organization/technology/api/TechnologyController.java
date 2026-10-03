package com.arinno.canopus.organization.technology.api;



import java.util.List;

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

import com.arinno.canopus.organization.technology.api.mapper.TechnologyMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.technology.application.ITechnologyService;
import com.arinno.canopus.services.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.technology.domain.Technology;
import com.arinno.canopus.organization.technology.contract.TechnologyRequest;
import com.arinno.canopus.organization.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.user.domain.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/technology")
public class TechnologyController {

	private final ITechnologyService technologyService; 
	private final JwtService jwtService;
	private final UserService userService;
	private final TechnologyMapper technologyMapper;

    public TechnologyController(
			ITechnologyService technologyService, 
			JwtService jwtService, 
			UserService userService, 
			TechnologyMapper technologyMapper) {
		this.technologyService = technologyService;
		this.jwtService = jwtService;
		this.userService = userService;
		this.technologyMapper = technologyMapper;
	}

	@GetMapping
	public ResponseEntity<List<TechnologyResponse>> list(@RequestHeader(value="Authorization") String auth) {	
		List<TechnologyResponse> technologies = technologyService.findByCompany(jwtService.getCompanyFromToken(auth))
				.stream()
				.map(technologyMapper::toSummary)
				.toList();
		return ResponseEntity.ok(technologies);
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
		return ResponseEntity.ok(technologyMapper.toDetail(technology));
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

