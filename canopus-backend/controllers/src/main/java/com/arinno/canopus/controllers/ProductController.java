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
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.controllers.mapper.ProductMapper;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.ProductResponse;
import com.arinno.canopus.entities.ProductRequest;
import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.ProductDataIntegrityException;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.ITechnologyService;
import com.arinno.canopus.servicies.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService; 

	private final JwtService jwtService;

		private final ITechnologyService technologyService;

		private final UserService userService;

		private final ProductMapper productMapper;

		ProductController(IProductService productService, JwtService jwtService, ITechnologyService technologyService, UserService userService, ProductMapper productMapper) {
		this.productService = productService;
		this.jwtService = jwtService;
		this.technologyService = technologyService;
		this.userService = userService;
		this.productMapper = productMapper;
	}

	@GetMapping
	public List<ProductResponse> list(@RequestHeader(value="Authorization") String auth){	
		return productService.findByCompany(jwtService.getCompanyFromToken(auth)).stream()
			.map(productMapper::toSummary)
			.toList();
	}

	@GetMapping("/responsible")
	public List<ProductResponse> listResponsibleMe(@RequestHeader(value="Authorization") String auth){	
		return productService.findByResponsibleAndCompany(jwtService.getUserFromToken(auth), jwtService.getCompanyFromToken(auth)).stream()
			.map(productMapper::toSummary)
			.toList();
	}

	@GetMapping("/responsible-or-backup")
	public List<ProductResponse> listResponsibleOrBackupMe(@RequestHeader(value="Authorization") String auth){
		Company company = jwtService.getCompanyFromToken(auth);
		return productService.findByResponsibleOrBackupAndCompany(jwtService.getUserFromToken(auth).getId(), company.getId()).stream()
			.map(productMapper::toSummary)
			.toList();
	}

	@GetMapping("/technology/{id}")
	public List<ProductResponse> listTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth){	
		Technology technology = new Technology();
		technology.setId(id);
		return productService.findByTechnologyAndCompany(technology, jwtService.getCompanyFromToken(auth)).stream().map(productMapper::toSummary).toList();
	}
	
	@GetMapping("/contributor/{id}")
	public List<ProductResponse> ListByContributor(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		User user = new User();
		user.setId(id);
		return productService.findByContributorAndCompany(id, jwtService.getCompanyFromToken(auth).getId())
		.stream()
		.map(product -> productMapper.toResponseForUser(product, user))
		.toList();		
	}

	@GetMapping("/{id}")
	public ProductResponse product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
		Product product = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		return productMapper.toDetail(product);
	}	
		
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Product update(@Valid @RequestBody ProductRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Product productDb = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
			if (productDb == null || productDb.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado para la empresa.");
			}
			applyRequest(productDb, request, jwtService.getCompanyFromToken(auth));
			return productService.save(productDb);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ProductDataIntegrityException();
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
	
	@PostMapping	
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@Valid @RequestBody ProductRequest request, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Company company = jwtService.getCompanyFromToken(auth);
			Product product = new Product();
			applyRequest(product, request, company);
			product.setCompany(company);
			productService.save(product);
		} catch (DataIntegrityViolationException e) {
			throw new ProductDataIntegrityException();
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

	private void applyRequest(Product product, ProductRequest request, Company company) {
		Technology technology = technologyService.findByIdAndCompany(request.getTechnologyId(), company);
		User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company).orElse(null);
		User backup = userService.findByIdAndCompany(request.getBackupId(), company).orElse(null);

		if (technology.getId() == null || responsible == null || backup == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tecnología, responsable o backup no válido para la empresa.");
		}

		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setTechnology(technology);
		product.setResponsible(responsible);
		product.setBackup(backup);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws Exception {
		try {
			Product product = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
			if (product == null || product.getId() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado para la empresa.");
			}
			productService.deleteByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
		} catch (ResponseStatusException e) {
			throw e;
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


