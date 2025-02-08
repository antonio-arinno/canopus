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

import com.arinno.canopus.entities.Product;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.util.IUtil;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
	private IProductService productService; 

	@Autowired
	private IUtil util;

	
    @GetMapping
	public List<Product> list(@RequestHeader(value="Authorization") String auth){	
		return productService.findByCompany(util.getCompany(auth));
	}		
	

	@GetMapping("/contributor")
	public List<Product> listNotProductionContributor(@RequestHeader(value="Authorization") String auth){
		return productService.findByProjectNotProductionAndContributor(util.getUser(auth).getId());
	}	

	@GetMapping("/{id}")
	public Product product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		return productService.findByIdAndCompany(id, util.getCompany(auth));
	}

/*
	@GetMapping("/{id}")
	public ProductResponse product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		ProductResponse productResponse = new ProductResponse();
		Product product = productService.findByIdAndCompany(id, util.getCompany(auth));
		productResponse.setId(product.getId());
		productResponse.setName(product.getName());
		productResponse.setDescription(product.getDescription());
		productResponse.setCreateAt(product.getCreateAt());
		productResponse.setTime(product.getTime());
		UserRequest userRequest = new UserRequest();
		productResponse.setResponsible(userRequest);
		productResponse.getResponsible().setId(product.getResponsible().getId());
		productResponse.getResponsible().setName(product.getResponsible().getName());
		return productResponse;
	}
 */		
		
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Product update(@RequestBody Product product, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product productDb = productService.findByIdAndCompany(id, util.getCompany(auth));
		productDb.setName(product.getName());
		productDb.setDescription(product.getDescription());
		productDb.setTechnology(product.getTechnology());
		productDb.setResponsible(product.getResponsible());
		return productService.save(productDb);
	}	
	
	@PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public Product create(@RequestBody Product product, @RequestHeader(value="Authorization") String auth) {
		product.setCompany(util.getCompany(auth));
		return productService.save(product);
	}	

	@DeleteMapping("/{id}")	
//	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		productService.deleteByIdAndCompany(id, util.getCompany(auth));
	}		

	@GetMapping("/select/{term}")
	public List<Product> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return productService.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth));
	}	
}
