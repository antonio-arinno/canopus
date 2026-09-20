package com.arinno.canopus.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.arinno.canopus.controllers.mapper.ImputationMapper;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationRequest;
import com.arinno.canopus.entities.ImputationResponse;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.servicies.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/imputation")
public class ImputationController {
  
	private final IImputationService imputationService;
	private final JwtService jwtService;
	private final ImputationMapper imputationMapper;
	private final IProjectService projectService;

	ImputationController(
			IImputationService imputationService, 
			JwtService jwtService, 
			ImputationMapper imputationMapper,
			IProjectService projectService) {
		this.imputationService = imputationService;
		this.jwtService = jwtService;
		this.imputationMapper = imputationMapper;
		this.projectService = projectService;
	}

	@GetMapping	
	public ResponseEntity<List<ImputationResponse>> list(@RequestHeader(value="Authorization") String auth) {	
		List<ImputationResponse> imputations = imputationService.findByUser(jwtService.getUserFromToken(auth))
				.stream()
				.map(imputationMapper::toResponse)
				.toList();
		return ResponseEntity.ok(imputations);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ImputationResponse> imputation(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Imputation imputation = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada."));
	
		return ResponseEntity.ok(imputationMapper.toDetailResponse(imputation));
	}

	@GetMapping("/date/{dateString}")
	public ResponseEntity<ImputationResponse> imputationByDate(@PathVariable String dateString, @RequestHeader(value="Authorization") String auth) {
		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha debe tener el formato yyyy-MM-dd.");
		}
		
		Imputation imputation = imputationService.findByDateAndUser(date, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada para la fecha indicada."));
		
		return ResponseEntity.ok(imputationMapper.toDetailResponse(imputation));
	}

	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody ImputationRequest request, @RequestHeader(value="Authorization") String auth) {
		Imputation imputation = toCompanyScopedImputation(request, auth);
		imputation.setUser(jwtService.getUserFromToken(auth));
		imputationService.save(imputation);
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@Valid @RequestBody ImputationRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Imputation imputationDb = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada."));
		
		Imputation imputation = toCompanyScopedImputation(request, auth);
		imputationService.deleteByIdAndUser(imputationDb.getId(), jwtService.getUserFromToken(auth));
		imputation.setUser(jwtService.getUserFromToken(auth));
		imputationService.save(imputation);
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Imputation imputation = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada."));
		
		imputationService.deleteByIdAndUser(imputation.getId(), jwtService.getUserFromToken(auth));
		return ResponseEntity.noContent().build();
	}	

	@GetMapping("/product/{id}")
	public ResponseEntity<List<Map<String, Object>>> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {	
		List<Map<String, Object>> summaries = imputationService.findByProductAndCompany(id, jwtService.getCompanyFromToken(auth).getId());
		return ResponseEntity.ok(summaries);
	}	

	@GetMapping("/project/{id}")
	public ResponseEntity<List<Map<String, Object>>> findByProject(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {	
		List<Map<String, Object>> summaries = imputationService.findByProjectAndCompany(id, jwtService.getCompanyFromToken(auth).getId());
		return ResponseEntity.ok(summaries);
	}

	private Imputation toCompanyScopedImputation(ImputationRequest request, String auth) {
		Company company = jwtService.getCompanyFromToken(auth);
		List<Project> projects = request.getItems().stream().map(item -> {
			Project project = projectService.findByIdAndCompany(item.getProjectId(), company);
			if (project == null || project.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado o no válido para la empresa.");
			}
			return project;
		}).toList();
		return imputationMapper.toEntity(request, projects);
	}
}
