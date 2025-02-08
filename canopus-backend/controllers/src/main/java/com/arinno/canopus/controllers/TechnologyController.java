package com.arinno.canopus.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.servicies.ITechnologyService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/technology")
public class TechnologyController {

    @Autowired
	private ITechnologyService technologyService; 

	@Autowired
	private IUtil util;

    @GetMapping
	public List<Technology> list(@RequestHeader(value="Authorization") String auth){	
		return technologyService.findByCompany(util.getCompany(auth));
	}		

    @PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public Technology save(@RequestBody Technology technology, @RequestHeader(value="Authorization") String auth) {
		technology.setCompany(util.getCompany(auth));
		return technologyService.save(technology);
	}	

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		technologyService.deleteByIdAndCompany(id, util.getCompany(auth));
	}	
	
	@GetMapping("/{id}")
	public Technology getTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		return technologyService.findByIdAndCompany(id, util.getCompany(auth));
	}

	@GetMapping("/select/{term}")
	public List<Technology> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return technologyService.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth));
	}	

}
