package com.arinno.canopus.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;


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

import com.arinno.canopus.controllers.mapper.ImputationMapper;
import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationResponse;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.JwtService;

@RestController
@RequestMapping("/imputation")
public class ImputationController {
  
	private final IImputationService imputationService;

	private final JwtService jwtService;

	private final ImputationMapper imputationMapper;

	ImputationController(IImputationService imputationService, JwtService jwtService, ImputationMapper imputationMapper) {
		this.imputationService = imputationService;
		this.jwtService = jwtService;
		this.imputationMapper = imputationMapper;
	}

	@GetMapping	
	public List<ImputationResponse> list(@RequestHeader(value="Authorization") String auth){	
		return imputationService.findByUser(jwtService.getUserFromToken(auth)).stream().map(imputationMapper::toResponse).toList();			
	}

	@GetMapping("/{id}")
	public ImputationResponse imputation(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		
		Imputation imputation = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputation not found"));
	
		return imputationMapper.toDetailResponse(imputation);
	}

	
	@GetMapping("/date/{dateString}")
	public ImputationResponse imputationByDate(@PathVariable String dateString, @RequestHeader(value="Authorization") String auth) {
		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha debe tener el formato yyyy-MM-dd.");
		}
		Imputation imputation =  imputationService.findByDateAndUser(date, jwtService.getUserFromToken(auth))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputation not found"));
		
		return imputationMapper.toDetailResponse(imputation);

	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody Imputation imputation, @RequestHeader(value="Authorization") String auth) throws CustomException {
		try {
			imputation.setUser(jwtService.getUserFromToken(auth));
			imputationService.save(imputation);
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
	
	@GetMapping("/createcal")
	@ResponseStatus(HttpStatus.CREATED)
	public void createCal(@RequestHeader(value="Authorization") String auth) {

		ZoneId defaultZoneId = ZoneId.systemDefault();
		for (LocalDate day = LocalDate.parse("2023-01-01"); day.getYear() < 2024; day = day.plusDays(1)) {
			Imputation imputation = new Imputation();
			imputation.setUser(jwtService.getUserFromToken(auth));
			imputation.setDate(Date.from(day.atStartOfDay(defaultZoneId).toInstant()));
			imputationService.save(imputation);
		}
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public void update(@RequestBody Imputation imputation, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws CustomException {
		try {
			Imputation imputationDb = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada."));
			imputationService.deleteByIdAndUser(imputationDb.getId(), jwtService.getUserFromToken(auth));
			imputation.setId(null);
			imputation.getItems().forEach(item -> item.setId(null));
			imputation.setUser(jwtService.getUserFromToken(auth));
			imputationService.save(imputation);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws CustomException {
		try {
			Imputation imputation = imputationService.findByIdAndUser(id, jwtService.getUserFromToken(auth))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imputación no encontrada."));
			imputationService.deleteByIdAndUser(imputation.getId(), jwtService.getUserFromToken(auth));
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}	

	@GetMapping("/product/{id}")
	public List<Map<String, Object>> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		return imputationService.findByProductAndCompany(id, jwtService.getCompanyFromToken(auth).getId());
	}	

	@GetMapping("/project/{id}")
	public List<Map<String, Object>> findByProject(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		return imputationService.findByProjectAndCompany(id, jwtService.getCompanyFromToken(auth).getId());
	}


}
