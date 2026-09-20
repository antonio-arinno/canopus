package com.arinno.canopus.controllers;

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
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.TechnologyMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyRequest;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.servicies.ITechnologyService;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/technology")
public class TechnologyController {

	private final ITechnologyService technologyService; 
	private final JwtService jwtService;
	private final UserService userService;
	private final TechnologyMapper technologyMapper;

	TechnologyController(
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
	public ResponseEntity<Void> save(@Valid @RequestBody TechnologyRequest request, @RequestHeader(value="Authorization") String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		Technology technology = technologyMapper.toEntity(request);
		applyCompanyScopedResponsible(technology, request, company);
		technology.setCompany(company);
		technologyService.save(technology);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}	

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@Valid @RequestBody TechnologyRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Technology technologyDb = technologyService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		if (technologyDb == null || technologyDb.getId() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tecnología no encontrada para la empresa.");
		}
		Company company = jwtService.getCompanyFromToken(auth);
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
