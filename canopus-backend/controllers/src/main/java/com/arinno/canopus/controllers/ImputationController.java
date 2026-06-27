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

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationItem;
import com.arinno.canopus.entities.ImputationItemResponse;
import com.arinno.canopus.entities.ImputationResponse;
import com.arinno.canopus.entities.ProjectResponse;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/imputation")
public class ImputationController {
  
	private final IImputationService imputationService;

	private final IUtil util;

	ImputationController(IImputationService imputationService, IUtil util) {
		this.imputationService = imputationService;
		this.util = util;
	}
/*	
	@GetMapping	
	public List<Imputation> list(@RequestHeader(value="Authorization") String auth){	
		return imputationService.findByUser(util.getUser(auth));	
	}
*/
	@GetMapping	
	public List<ImputationResponse> list(@RequestHeader(value="Authorization") String auth){	
		return imputationService.findByUser(util.getUser(auth)).stream().map(imputation -> GetImputationResponse(imputation)).toList();			
	}

	private ImputationResponse GetImputationResponse(Imputation imputation){
		return ImputationResponse.builder()
				.id(imputation.getId())
				.date(imputation.getDate())
				.time(imputation.getTotal())
				.build();
	}
/*
	@GetMapping("/{id}")
	public Imputation imputation(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		return imputationService.findByIdAndUser(id, util.getUser(auth));
	}
*/
	@GetMapping("/{id}")
	public ImputationResponse imputation(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Imputation imputation = imputationService.findByIdAndUser(id, util.getUser(auth));

		return ImputationResponse.builder()
				.id(imputation.getId())
				.date(imputation.getDate())
				.time(imputation.getTotal())
				.items(imputation.getItems().stream().map(item -> GetImputationItemResponse(item)).toList())
				.build();
	}

	
	@GetMapping("/date/{dateString}")
	public ImputationResponse imputationByDate(@PathVariable String dateString, @RequestHeader(value="Authorization") String auth) {		 
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}  	
		Imputation imputation =  imputationService.findByDateAndUser(date, util.getUser(auth));
		
		return ImputationResponse.builder()
				.id(imputation.getId())
				.date(imputation.getDate())
				.time(imputation.getTotal())
				.items(imputation.getItems().stream().map(item -> GetImputationItemResponse(item)).toList())
				.build();
		
	}
	
	private ImputationItemResponse GetImputationItemResponse (ImputationItem item){
		return ImputationItemResponse.builder()
				.id(item.getId())
				.time(item.getTime())
				.project(ProjectResponse.builder()
							.id(item.getProject().getId())
							.name(item.getProject().getName())
							.build())
				.build();

	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody Imputation imputation, @RequestHeader(value="Authorization") String auth) {
		imputation.setUser(util.getUser(auth));
		imputationService.save(imputation);
	}
	
	@GetMapping("/createcal")
	@ResponseStatus(HttpStatus.CREATED)
	public void createCal(@RequestHeader(value="Authorization") String auth) {

		ZoneId defaultZoneId = ZoneId.systemDefault(); 
        for ( LocalDate day = LocalDate.parse("2023-01-01"); day.getYear() < 2024 ; day = day.plusDays(1)) {
    		Imputation imputation = new Imputation();
    		imputation.setUser(util.getUser(auth));
    		Date date = Date.from(day.atStartOfDay(defaultZoneId).toInstant());
    		imputation.setDate(date);
    		imputationService.save(imputation);
        }		
	}	
	
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public void update(@RequestBody Imputation imputation, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws CustomException {

		imputationService.deleteByIdAndUser(id, util.getUser(auth));
		imputation.setId(null);
		imputation.getItems().stream().map(item -> {item.setId(null); return item;}).toList();
		imputation.setUser(util.getUser(auth));
		try {
			imputationService.save(imputation);
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}	
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		imputationService.deleteByIdAndUser(id, util.getUser(auth));
	}	

	@GetMapping("/product/{id}")
	public List<Map<String, Object>> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		return imputationService.findByProduct(id);
	}	

	@GetMapping("/project/{id}")
	public List<Map<String, Object>> findByProject(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		return imputationService.findByProject(id);
	}


}
