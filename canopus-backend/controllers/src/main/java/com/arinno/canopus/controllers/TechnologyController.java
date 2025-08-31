package com.arinno.canopus.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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


import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.error.TechnologyDataIntegrityException;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.servicies.ITechnologyService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/technology")
public class TechnologyController {

    @Autowired
	private ITechnologyService technologyService; 

	@Autowired
	private IProductService productService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IImputationService imputationService;


	@Autowired
	private IUtil util;

    @GetMapping
	public List<TechnologyResponse> list(@RequestHeader(value="Authorization") String auth){	
		return technologyService.findByCompany(util.getCompany(auth)).stream().map(technology -> GetTechnologyResponse(technology)).toList();
	}		

	private TechnologyResponse GetTechnologyResponse (Technology technology){
		return TechnologyResponse.builder()
					.id(technology.getId())
					.name(technology.getName())
					.description(technology.getDescription())
					.countProducts(productService.countByTechnology(technology))
					.countProjects(projectService.countByTechnology(technology.getId()))
					.countContributors(projectService.countContributorsByTechnology(technology.getId()))
					.time(imputationService.timeByTechnology(technology))
					.build();
	}


    @PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public void save(@RequestBody Technology technology, @RequestHeader(value="Authorization") String auth) {
		technology.setCompany(util.getCompany(auth));
		technologyService.save(technology);
	}	

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public void update(@RequestBody Technology technology, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {	
		technology.setCompany(util.getCompany(auth));
		technologyService.save(technology);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			technologyService.deleteByIdAndCompany(id, util.getCompany(auth));
		} catch (DataIntegrityViolationException e) {
			throw new TechnologyDataIntegrityException();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}	
	
	@GetMapping("/{id}")
	public TechnologyResponse getTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Technology technology = technologyService.findByIdAndCompany(id, util.getCompany(auth));

		return TechnologyResponse.builder()
			.id(technology.getId())
			.name(technology.getName())
			.description(technology.getDescription())
			.responsible(UserResponse.builder()
				.id(technology.getResponsible().getId())
				.name(technology.getResponsible().getName())
				.lastname(technology.getResponsible().getLastname())
				.build())
			.countProducts(productService.countByTechnology(technology))
			.countProjects(projectService.countByTechnology(technology.getId()))
			.countContributors(projectService.countContributorsByTechnology(technology.getId()))
			.time(imputationService.timeByTechnology(technology))		
			.build();
	}

	@GetMapping("/select/{term}")
	public List<Technology> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return technologyService.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth));
	}	

}
