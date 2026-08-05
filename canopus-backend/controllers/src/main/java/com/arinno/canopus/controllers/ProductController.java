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

import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.ProductResponse;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.ProductDataIntegrityException;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.IProjectService;
import com.arinno.canopus.servicies.JwtService;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService; 

	private final IProjectService projectService;

	private final IImputationService imputationService;

	private final JwtService jwtService;

	ProductController(IProductService productService, IProjectService projectService, IImputationService imputationService, JwtService jwtService) {
		this.productService = productService;
		this.projectService = projectService;
		this.imputationService = imputationService;
		this.jwtService = jwtService;
	}

	@GetMapping
	public List<ProductResponse> list(@RequestHeader(value="Authorization") String auth){	
		return productService.findByCompany(jwtService.getCompanyFromToken(auth)).stream()
			.map(product -> toProductResponse(product))
			.toList();
	}

	@GetMapping("/responsible")
	public List<ProductResponse> listResponsibleMe(@RequestHeader(value="Authorization") String auth){	
		return productService.findByResponsible(jwtService.getUserFromToken(auth)).stream()
			.map(product -> toProductResponse(product))
			.toList();
	}

	@GetMapping("/technology/{id}")
	public List<ProductResponse> listTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		Technology technology = new Technology();
		technology.setId(id);
		return productService.findByTechnology(technology).stream().map(product -> toProductResponse(product)).toList();
	}
	
	@GetMapping("/contributor/{id}")
	public List<ProductResponse> ListByContributor(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		User user = new User();
		user.setId(id);
		return productService.findByContributorAndCompany(id, jwtService.getCompanyFromToken(auth).getId())
		.stream()
		.map(product -> GetProductResponseForUser(product, user))
		.toList();		
	}

	private ProductResponse GetProductResponseForUser(Product product, User user) {
		return ProductResponse.builder()
				.name(product.getName())
				.description(product.getDescription())
				.countProjects(projectService.countByProduct(product))
				.countContributors(projectService.countContributorsByProduct(product.getId()))
				.technology(TechnologyResponse.builder()
								.name(product.getTechnology().getName())
								.build())
				.responsible(UserResponse.builder()
								.name(product.getResponsible().getName())
								.build())
				.time(imputationService.timeByProductAndUser(product, user))				
				.build();
	}

private ProductResponse toProductResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.technology(TechnologyResponse.builder()
								.id(product.getTechnology().getId())
								.name(product.getTechnology().getName())
								.build())	
				.responsible(UserResponse.builder()
								.id(product.getResponsible().getId())
								.name(product.getResponsible().getName())
								.lastname(product.getResponsible().getLastname())
								.build())
				.backup(UserResponse.builder()
								.id(product.getBackup().getId())
								.name(product.getBackup().getName())
								.lastname(product.getBackup().getLastname())
								.build())		
				.time(imputationService.timeByProduct(product))				
				.build();
	}

	@GetMapping("/{id}")
	public ProductResponse product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));

		return ProductResponse.builder()
		.id(product.getId())
		.name(product.getName())
		.description(product.getDescription())
		.technology(TechnologyResponse.builder()
						.id(product.getTechnology().getId())
						.name(product.getTechnology().getName())
						.build())	
		.responsible(UserResponse.builder()
						.id(product.getResponsible().getId())
						.name(product.getResponsible().getName())
						.lastname(product.getResponsible().getLastname())
						.build())
		.backup(UserResponse.builder()
						.id(product.getBackup().getId())
						.name(product.getBackup().getName())
						.lastname(product.getBackup().getLastname())
						.build())				
		.countProjects(projectService.countByProduct(product))
		.countContributors(projectService.countContributorsByProduct(product.getId()))
		.time(imputationService.timeByProduct(product))		
		.avgTime(imputationService.avgTimeByProduct(product))
		.avgDuration(imputationService.avgDurationByProduct(product))			
		.build();
	}	
		
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Product update(@RequestBody Product product, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product productDb = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		productDb.setName(product.getName());
		productDb.setDescription(product.getDescription());
		productDb.setTechnology(product.getTechnology());
		productDb.setResponsible(product.getResponsible());
		productDb.setBackup(product.getBackup());
		return productService.save(productDb);
	}	
	
	@PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody Product product, @RequestHeader(value="Authorization") String auth) {
		product.setCompany(jwtService.getCompanyFromToken(auth));
		productService.save(product);
	}	

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			productService.deleteByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		} catch (DataIntegrityViolationException e) {
			throw new ProductDataIntegrityException();
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

	@GetMapping("/select/{term}")
	public List<Product> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return productService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
	}	
}


