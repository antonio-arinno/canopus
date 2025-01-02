package com.arinno.canopus.controllers;

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

import com.arinno.canopus.entities.Project;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/project")
public class ProjectController {

	@Autowired
	private IProjectService projectService;

    @Autowired
	private IUtil util;
 	
	@GetMapping	
	public List<Project> list(@RequestHeader(value="Authorization") String auth){
		return projectService.findByCompany(util.getCompany(auth));
	}

	@GetMapping("/{id}")
	public Project project(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		return projectService.findByIdAndCompany(id, util.getCompany(auth));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Project create(@RequestBody Project project, @RequestHeader(value="Authorization") String auth) {
		project.setCompany(util.getCompany(auth));	
		return projectService.save(project);
	}
	
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Project edit(@RequestBody Project project, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Project projectDb = projectService.findByIdAndCompany(id, util.getCompany(auth));
		projectDb.setName(project.getName());
		projectDb.setDescription(project.getDescription());
		projectDb.setProduct(project.getProduct());
		projectDb.setStatus(project.getStatus());
		projectDb.setResponsible(project.getResponsible());
		projectDb.setContributors(project.getContributors());
		return projectService.save(projectDb);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		projectService.deleteByIdAndCompany(id, util.getCompany(auth));
	}	
	
	@GetMapping("/select/{term}")
	public List<Project> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return projectService.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth));
	}				


}
