package com.arinno.canopus.controllers;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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


import com.arinno.canopus.controllers.mapper.TechnologyMapper;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyRequest;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.TechnologyDataIntegrityException;
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

	TechnologyController(ITechnologyService technologyService, JwtService jwtService, UserService userService, TechnologyMapper technologyMapper) {
		this.technologyService = technologyService;
		this.jwtService = jwtService;
		this.userService = userService;
		this.technologyMapper = technologyMapper;
	}

    @GetMapping
	public List<TechnologyResponse> list(@RequestHeader(value="Authorization") String auth){	
		return technologyService.findByCompany(jwtService.getCompanyFromToken(auth)).stream().map(technologyMapper::toSummary).toList();
	}

    @PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public void save(@Valid @RequestBody TechnologyRequest request, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Company company = jwtService.getCompanyFromToken(auth);
			Technology technology = technologyMapper.toEntity(request);
			applyCompanyScopedResponsible(technology, request, company);
			technology.setCompany(company);
			technologyService.save(technology);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}	

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public void update(@Valid @RequestBody TechnologyRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
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
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

	private void applyCompanyScopedResponsible(Technology technology, TechnologyRequest request, Company company) {
		User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company).orElse(null);
		if (responsible == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Responsable no válido para la empresa.");
		}
		technology.setResponsible(responsible);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Technology technology = technologyService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
			if (technology == null || technology.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tecnología no encontrada para la empresa.");
			}
			technologyService.deleteByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new TechnologyDataIntegrityException();
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}	
	
	@GetMapping("/{id}")
	public TechnologyResponse getTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Technology technology = technologyService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		if (technology == null || technology.getId() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tecnología no encontrada para la empresa.");
		}
		return technologyMapper.toDetail(technology);
	}

	@GetMapping("/select/{term}")
	public List<Technology> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return technologyService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
	}	

}
