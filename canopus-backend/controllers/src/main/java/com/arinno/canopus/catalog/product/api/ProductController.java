package com.arinno.canopus.catalog.product.api;



import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.catalog.product.api.mapper.ProductMapper;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.product.contract.ProductResponse;
import com.arinno.canopus.catalog.product.contract.ProductRequest;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.organization.technology.application.ITechnologyService;
import com.arinno.canopus.services.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService; 
    private final JwtService jwtService;
    private final ITechnologyService technologyService;
    private final UserService userService;
    private final ProductMapper productMapper;

    ProductController(
            IProductService productService, 
            JwtService jwtService, 
            ITechnologyService technologyService, 
            UserService userService, 
            ProductMapper productMapper) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.technologyService = technologyService;
        this.userService = userService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@RequestHeader(value="Authorization") String auth) {	
        List<ProductResponse> products = productService.findByCompany(jwtService.getCompanyFromToken(auth))
                .stream()
                .map(productMapper::toSummary)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/responsible")
    public ResponseEntity<List<ProductResponse>> listResponsibleMe(@RequestHeader(value="Authorization") String auth) {	
        List<ProductResponse> products = productService.findByResponsibleAndCompany(
                jwtService.getUserFromToken(auth), 
                jwtService.getCompanyFromToken(auth))
                .stream()
                .map(productMapper::toSummary)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/responsible-or-backup")
    public ResponseEntity<List<ProductResponse>> listResponsibleOrBackupMe(@RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        List<ProductResponse> products = productService.findByResponsibleOrBackupAndCompany(
                jwtService.getUserFromToken(auth).getId(), 
                company.getId())
                .stream()
                .map(productMapper::toSummary)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/technology/{id}")
    public ResponseEntity<List<ProductResponse>> listTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {	
        Technology technology = new Technology();
        technology.setId(id);
        List<ProductResponse> products = productService.findByTechnologyAndCompany(technology, jwtService.getCompanyFromToken(auth))
                .stream()
                .map(productMapper::toSummary)
                .toList();
        return ResponseEntity.ok(products);
    }
	
    @GetMapping("/contributor/{id}")
    public ResponseEntity<List<ProductResponse>> listByContributor(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        User user = new User();
        user.setId(id);
        List<ProductResponse> products = productService.findByContributorAndCompany(id, jwtService.getCompanyFromToken(auth).getId())
                .stream()
                .map(product -> productMapper.toResponseForUser(product, user))
                .toList();		
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Product product = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(productMapper.toDetail(product));
    }
		
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@Valid @RequestBody ProductRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        Product productDb = productService.findByIdAndCompany(id, company);
        
        applyRequest(productDb, request, company);
        Product savedProduct = productService.save(productDb);
        
        // CORRECCIÓN: Devolvemos un estado HTTP 200 OK semántico para actualizaciones exitosas
        return ResponseEntity.ok(productMapper.toDetail(savedProduct));
    }
	
    @PostMapping	
    public ResponseEntity<Void> create(@Valid @RequestBody ProductRequest request, @RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        Product product = new Product();
        
        applyRequest(product, request, company);
        product.setCompany(company);
        productService.save(product);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        productService.deleteByIdAndCompany(id, company);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/select/{term}")
    public ResponseEntity<List<Product>> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth) {	
        List<Product> products = productService.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(products);
    }	

    private void applyRequest(Product product, ProductRequest request, Company company) {
        Technology technology = technologyService.findByIdAndCompany(request.getTechnologyId(), company);
        User responsible = userService.findByIdAndCompany(request.getResponsibleId(), company);
        User backup = userService.findByIdAndCompany(request.getBackupId(), company);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setTechnology(technology);
        product.setResponsible(responsible);
        product.setBackup(backup);
    }
}



