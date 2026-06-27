package com.arinno.canopus.servicies;


import java.util.List;

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
        return (List<Product>) productRepository.findByCompany(company);
    }

	@Override
	@Transactional(readOnly = true)
	public List<Product> findByResponsible(User user) {
		return (List<Product>) productRepository.findByResponsible(user);
	}

    @Override
	@Transactional(readOnly = true)
	public List<Product> findByProjectNotProductionAndContributorAndCompany(Long id, Long company_id) {	
/*		
	    List<Product> productsTMP = productRepository.findByNotProductionAndContributorAndCompany(id, company_id);
	    List<Product> products = new ArrayList<Product>();
	    
	    for (Product productTMP : productsTMP) {
	    	System.out.println(productTMP.getName());
	    	Product product = new Product();
	    	product.setId(productTMP.getId());
	    	product.setName(productTMP.getName());
	    	product.setDescription(productTMP.getDescription());
	    	product.setResponsible(productTMP.getResponsible());
	    	product.setCreateAt(productTMP.getCreateAt());
	    	List<Project> projects = new ArrayList<Project>();
	    	for (Project projectTMP: productTMP.getProjects()){
	    		System.out.println(projectTMP.getStatus());
	    		if (projectTMP.getStatus() != Status.PRODUCTION) {
	    			for (User contributorTMP: projectTMP.getContributors()) {
	    				if (contributorTMP.getId().equals(id)) {	    			
			    			Project project = new Project();
			    			project.setId(projectTMP.getId());
			    			project.setName(projectTMP.getName());
			    			project.setDescription(projectTMP.getDescription());
			    			project.setResponsible(projectTMP.getResponsible());
			    			project.setContributors(projectTMP.getContributors());
			    			project.setStatus(projectTMP.getStatus());
			    			project.setCreateAt(projectTMP.getCreateAt());
			    			project.setCompany(projectTMP.getCompany());
			    			projects.add(project);
	    				}
	    			}
	    		}	    		
	    		product.setProjects(projects);
	    	}
	    	products.add(product);
	    }			
		return products;
*/		
		return null;
	}		

    @Override
	@Transactional(readOnly = true)
	public List<Product> findByProjectNotProductionAndContributor(Long id) {			
/*
	    List<Product> productsTMP = productRepository.findByNotProductionAndContributor(id);
	    List<Product> products = new ArrayList<Product>();
	    
	    for (Product productTMP : productsTMP) {
	    	System.out.println(productTMP.getName());
	    	Product product = new Product();
	    	product.setId(productTMP.getId());
	    	product.setName(productTMP.getName());
	    	product.setDescription(productTMP.getDescription());
	    	product.setResponsible(productTMP.getResponsible());
	    	product.setCreateAt(productTMP.getCreateAt());
	    	List<Project> projects = new ArrayList<Project>();
	    	for (Project projectTMP: productTMP.getProjects()){
	    		System.out.println(projectTMP.getStatus());
	    		if (projectTMP.getStatus() != Status.PRODUCTION) {
	    			for (User contributorTMP: projectTMP.getContributors()) {
	    				if (contributorTMP.getId().equals(id)) {	    			
			    			Project project = new Project();
			    			project.setId(projectTMP.getId());
			    			project.setName(projectTMP.getName());
			    			project.setDescription(projectTMP.getDescription());
			    			project.setResponsible(projectTMP.getResponsible());
			    			project.setContributors(projectTMP.getContributors());
			    			project.setStatus(projectTMP.getStatus());
			    			project.setCreateAt(projectTMP.getCreateAt());
			    			project.setCompany(projectTMP.getCompany());
			    			projects.add(project);
	    				}
	    			}
	    		}	    		
	    		product.setProjects(projects);
	    	}
	    	products.add(product);
	    }
		return products;
*/
		return null;		
	}		
    
    @Override
    @Transactional(readOnly = true)
    public Product findByIdAndCompany(Long id, Company company) {
        return productRepository.findByIdAndCompany(id, company);
    }

    @Override
    @Transactional
    public void deleteByIdAndCompany(Long id, Company company) throws CustomException {
        productRepository.deleteByIdAndCompany(id, company);
    }
        
    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
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
	public List<Product> findByTechnology(Technology technology) {
		return productRepository.findByTechnology(technology);
	}

	@Override
	public List<Product> findByContributorAndCompany(Long id, Long id2) {
		return productRepository.findByContributorAndCompany(id, id2);
	}


}
