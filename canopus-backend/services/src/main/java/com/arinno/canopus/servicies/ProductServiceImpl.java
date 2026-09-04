package com.arinno.canopus.servicies;


import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

	ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
   
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCompany(Company company) {
        return productRepository.findByCompany(company);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Product findByIdAndCompany(Long id, Company company) {
        Product product = productRepository.findByIdAndCompany(id, company);
        return Objects.requireNonNullElse(product, new Product());
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) throws CustomException {
        productRepository.deleteByIdAndCompany(id, company);
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

	@Override
	public List<Product> findByContributorAndCompany(Long id, Long id2) {
		return productRepository.findByContributorAndCompany(id, id2);
	}


}
