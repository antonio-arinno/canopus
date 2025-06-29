package com.arinno.canopus.controllers;

import java.sql.Date;
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

import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.ProductResponse;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.ProjectResponse;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/project")
public class ProjectController {

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IImputationService imputationService;

    @Autowired
	private IUtil util;

	@GetMapping	
	public List<ProjectResponse> listPersonalOpened(@RequestHeader(value="Authorization") String auth){
		return projectService.findByResponsibleAndDateProIsNull(util.getUser(auth)).stream().map(project -> GetProjectResponse(project)).toList();
	}

	@GetMapping("/all")
	public List<ProjectResponse> listPersonalAll(@RequestHeader(value="Authorization") String auth){
		return projectService.findByResponsible(util.getUser(auth)).stream().map(project -> GetProjectResponse(project)).toList();
	}

	@GetMapping("/global")
	public List<ProjectResponse> listGlobalOpened(@RequestHeader(value="Authorization") String auth){
		return projectService.findByCompanyAndDateProIsNull(util.getCompany(auth)).stream().map(project -> GetProjectResponse(project)).toList();
	}

	@GetMapping("/globalall")
	public List<ProjectResponse> listGlobalAll(@RequestHeader(value="Authorization") String auth){
		return projectService.findByCompany(util.getCompany(auth)).stream().map(project -> GetProjectResponse(project)).toList();
	}

	@GetMapping("/product/{id}")
	public List<ProjectResponse> findByProduct(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = new Product();
		product.setId(id);
		return projectService.findByProduct(product).stream().map(project -> GetProjectResponse(project)).toList();
	}

	@GetMapping("/{id}")
	public ProjectResponse project(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Project project = projectService.findByIdAndCompany(id, util.getCompany(auth));
		return ProjectResponse.builder()
		.id(project.getId())
		.name(project.getName())
		.description(project.getDescription())
		.dateDev(project.getDateDev())
		.datePre(project.getDatePre())
		.datePro(project.getDatePro())
		.contributors(project.getContributors().stream().map(contributor -> GetContributorResponse(contributor)).toList())
		.responsible(UserResponse.builder()
			.id(project.getResponsible().getId())
			.name(project.getResponsible().getName())
			.build())		
		.product(ProductResponse.builder()
			.id(project.getProduct().getId())
			.name(project.getProduct().getName())
			.build())
		.technology(TechnologyResponse.builder()
			.id(project.getProduct().getTechnology().getId())
			.name(project.getProduct().getTechnology().getName())
			.build())	
		.countContributors(project.getContributors().size())					
		.time(imputationService.timeByProject(project))					
		.build();
	}
	
	private UserResponse GetContributorResponse (User contributor){
		return UserResponse.builder()
		.id(contributor.getId())
		.name(contributor.getName())
		.lastname(contributor.getLastname())
		.build();
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
		projectDb.setDateDev(project.getDateDev());
		projectDb.setDatePre(project.getDatePre());
		projectDb.setDatePro(project.getDatePro());
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
	
	@GetMapping("/contributor")
	public List<Project> listNotProductionContributor(@RequestHeader(value="Authorization") String auth){
		return projectService.findByContributorAndNotProduction(util.getUser(auth).getId());
	}	
	
	@GetMapping("/contributor/{date}")
	public List<ProjectResponse> listOpenedContributorDate(@PathVariable Date date, @RequestHeader(value="Authorization") String auth){
		return projectService.findByContributorOpenDate(util.getUser(auth).getId(), date).stream().map(project -> GetProjectResponse(project)).toList();
	}	
	
	private ProjectResponse GetProjectResponse(Project project) {

		return ProjectResponse.builder()
			.id(project.getId())
			.name(project.getName())
			.description(project.getDescription())
			.dateDev(project.getDateDev())
			.datePre(project.getDatePre())
			.datePro(project.getDatePro())
			.responsible(UserResponse.builder()
					.id(project.getResponsible().getId())
					.name(project.getResponsible().getName())
					.build())		
			.product(ProductResponse.builder()
					.id(project.getProduct().getId())
					.name(project.getProduct().getName())
					.build())
			.technology(TechnologyResponse.builder()
						.id(project.getProduct().getTechnology().getId())
						.name(project.getProduct().getTechnology().getName())
						.build())	
			.countContributors(project.getContributors().size())					
			.time(imputationService.timeByProject(project))				
			.build();

	}
}
