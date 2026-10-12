package com.arinno.canopus.catalog.product.application;


import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.catalog.product.infrastructure.persistence.ProductRepository;

@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

	ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Product> findPageByCompany(Company company, Pageable pageable) {
		return productRepository.findByCompany(company, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Product> findPageByResponsibleAndCompany(User user, Company company, Pageable pageable) {
		return productRepository.findByResponsibleAndCompany(user, company, pageable);
	}
   
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCompany(Company company) {
        return productRepository.findByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findByIdAndCompany(Long id, Company company) {
    	return productRepository.findByIdAndCompany(id, company)
            .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado para la empresa con ID: " + id));
    }

	@Transactional
	@Override
	public void deleteByIdAndCompany(Long id, Company company) {
		Product product = findByIdAndCompany(id, company); // Reutiliza el método funcional de arriba
		productRepository.delete(product);
	}

			
    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(Objects.requireNonNull(product, "product must not be null"));
    }

    @Override
	@Transactional
	public List<Product> findByNameContainingIgnoreCaseAndCompany(String term, Company company) {
		return productRepository.findByNameContainingIgnoreCaseAndCompany(term, company);
	}

	@Override
	@Transactional
	public Integer countByResponsible(User responsible) {
		return productRepository.countByResponsible(responsible);
	}

	@Override
	@Transactional
	public Integer countByTechnology(Technology technology) {
		return productRepository.countByTechnology(technology);
	}

	@Override
	public List<Product> findByResponsibleAndCompany(User user, Company company) {
		return productRepository.findByResponsibleAndCompany(user, company);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findByResponsibleOrBackupAndCompany(Long userId, Long companyId) {
		return productRepository.findByResponsibleOrBackupAndCompany(userId, companyId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findByTechnologyAndCompany(Technology technology, Company company) {
		return productRepository.findByTechnologyAndCompany(technology, company);
	}

}
