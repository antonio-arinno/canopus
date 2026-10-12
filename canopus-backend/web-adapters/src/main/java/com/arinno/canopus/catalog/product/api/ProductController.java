package com.arinno.canopus.catalog.product.api;



import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.catalog.product.api.mapper.ProductMapper;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.product.contract.ProductResponse;
import com.arinno.canopus.catalog.product.contract.ProductRequest;
import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.catalog.technology.application.ITechnologyService;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.reporting.application.ReportingService;
import com.arinno.canopus.reporting.contract.ProductMetrics;
import com.arinno.canopus.shared.contract.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService; 
    private final JwtService jwtService;
    private final ITechnologyService technologyService;
    private final UserService userService;
    private final ProductMapper productMapper;
    private final ReportingService reportingService;

    ProductController(
            IProductService productService, 
            JwtService jwtService, 
            ITechnologyService technologyService, 
            UserService userService, 
            ProductMapper productMapper,
            ReportingService reportingService) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.technologyService = technologyService;
        this.userService = userService;
        this.productMapper = productMapper;
        this.reportingService = reportingService;
    }

        @GetMapping("/page")
        public ResponseEntity<PageResponse<ProductResponse>> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "personal") String scope,
            @RequestHeader(value = "Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));
        Page<Product> products = "global".equalsIgnoreCase(scope)
            ? productService.findPageByCompany(company, pageable)
            : productService.findPageByResponsibleAndCompany(jwtService.getUserFromToken(auth), company, pageable);
        Map<Long, ProductMetrics> metricsByProduct = reportingService.productSummaryMetricsByIds(
            products.getContent().stream().map(Product::getId).toList());
        return ResponseEntity.ok(PageResponse.from(products,
            product -> productMapper.toSummary(product, metricsByProduct.get(product.getId()))));
        }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@RequestHeader(value="Authorization") String auth) {	
        List<Product> products = productService.findByCompany(jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toSummaries(products));
    }

    @GetMapping("/responsible")
    public ResponseEntity<List<ProductResponse>> listResponsibleMe(@RequestHeader(value="Authorization") String auth) {	
        List<Product> products = productService.findByResponsibleAndCompany(
                jwtService.getUserFromToken(auth), 
            jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toSummaries(products));
    }

    @GetMapping("/responsible-or-backup")
    public ResponseEntity<List<ProductResponse>> listResponsibleOrBackupMe(@RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        List<Product> products = productService.findByResponsibleOrBackupAndCompany(
                jwtService.getUserFromToken(auth).getId(), 
            company.getId());
        return ResponseEntity.ok(toSummaries(products));
    }

    @GetMapping("/technology/{id}")
    public ResponseEntity<List<ProductResponse>> listTechnology(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {	
        Technology technology = new Technology();
        technology.setId(id);
        List<Product> products = productService.findByTechnologyAndCompany(technology, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toSummaries(products));
    }
	
    @GetMapping("/contributor/{id}")
    public ResponseEntity<List<ProductResponse>> listByContributor(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        User user = new User();
        user.setId(id);
        List<Product> products = reportingService.findProductsByContributorAndCompany(
            id, jwtService.getCompanyFromToken(auth).getId());
    		Map<Long, ProductMetrics> metricsByProduct = reportingService.productMetricsForUserByIds(
    				products.stream().map(Product::getId).toList(), user.getId());
    		return ResponseEntity.ok(products.stream()
    				.map(product -> productMapper.toResponseForUser(product, user, metricsByProduct.get(product.getId())))
    				.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> product(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Product product = productService.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(productMapper.toDetail(product, reportingService.productMetrics(product)));
    }
		
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@Valid @RequestBody ProductRequest request, @PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Company company = jwtService.getCompanyFromToken(auth);
        Product productDb = productService.findByIdAndCompany(id, company);
        
        applyRequest(productDb, request, company);
        Product savedProduct = productService.save(productDb);
        
        // CORRECCIÓN: Devolvemos un estado HTTP 200 OK semántico para actualizaciones exitosas
        return ResponseEntity.ok(productMapper.toDetail(savedProduct, reportingService.productMetrics(savedProduct)));
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

        private List<ProductResponse> toSummaries(List<Product> products) {
        Map<Long, ProductMetrics> metricsByProduct = reportingService.productSummaryMetricsByIds(
            products.stream().map(Product::getId).toList());
        return products.stream()
            .map(product -> productMapper.toSummary(product, metricsByProduct.get(product.getId())))
            .toList();
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



