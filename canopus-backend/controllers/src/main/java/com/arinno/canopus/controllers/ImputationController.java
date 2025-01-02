package com.arinno.canopus.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/imputation")
public class ImputationController {
  
	@Autowired
	private IImputationService imputationService;

	@Autowired
    private IUtil util;
	
	@GetMapping	
	public List<Imputation> list(@RequestHeader(value="Authorization") String auth){	
		return imputationService.findByUser(util.getUser(auth));	
	}
	
	@GetMapping("/{id}")
	public Imputation imputation(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		return imputationService.findByIdAndUser(id, util.getUser(auth));
	}
	
	
	@GetMapping("/date/{dateString}")
	public Imputation imputationByDate(@PathVariable String dateString, @RequestHeader(value="Authorization") String auth) {		 
		Date date = null;
		System.out.println(date);
		System.out.println(dateString);
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
			System.out.println(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}  	
		return imputationService.findByDateAndUser(date, util.getUser(auth));
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Imputation create(@RequestBody Imputation imputation, @RequestHeader(value="Authorization") String auth) {
		System.out.println(imputation);
		imputation.setUser(util.getUser(auth));
		return imputationService.save(imputation);
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
	public Imputation update(@RequestBody Imputation imputation, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
/*
		Imputation imputationDb = imputationService.findByIdAndUser(id, getUser(auth));
		imputationService.deleteByIdAndUser(id, getUser(auth));
		imputationDb.setDate(imputation.getDate());
		imputationDb.setItems(imputation.getItems());
		return imputationService.save(imputationDb);
*/
/*
		imputationService.deleteByIdAndUser(id, util.getUser(auth));
		imputation.setUser(util.getUser(auth));
		return imputationService.save(imputation);
 */
		imputation.setId(id);
		imputation.setUser(util.getUser(auth));
		return imputationService.save(imputation);	
	}
	
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		imputationService.deleteByIdAndUser(id, util.getUser(auth));
	}	

}
