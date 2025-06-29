package com.arinno.canopus.servicies;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationItem;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.repositories.ImputationRepository;

@Service
public class ImputationServiceImpl implements IImputationService {

    @Autowired
	private ImputationRepository imputationRepository;

	@Override
	@Transactional(readOnly = true)	
	public List<Imputation> findByUser(User user) {
		return (List<Imputation>) imputationRepository.findByUser(user);
	}

	@Override
	public Imputation findByIdAndUser(Long id, User user) {
		return imputationRepository.findByIdAndUser(id, user);
	}	
	
	@Override
	public Imputation findByDateAndUser(Date date, User user) {
		return imputationRepository.findByDateAndUser(date, user);
	}	

	@Override
	@Transactional
	public Imputation save(Imputation imputation) {
		return imputationRepository.save(imputation);
	}
	
	@Override
	@Transactional
	public void deleteByIdAndUser(Long id, User user) {
		imputationRepository.deleteByIdAndUser(id, user);		
	}

	@Override
	@Transactional
	public Integer timeByProject(Project project) {
		Integer result = imputationRepository.timeByProject(project.getId());
		return (result == null) ? 0 : result; 	
	//	return (imputationRepository.timeByProject(project.getId()) == null) ? 0 : imputationRepository.timeByProject(project.getId()); 	
	}	

	@Override
	@Transactional
	public Integer timeByProduct(Product product) {
		Integer result = imputationRepository.timeByProduct(product.getId());
		return (result == null) ? 0 : result; 		
	}	

	@Override
	public Integer timeByProductAndUser(Product product, User user) {
		Integer result = 0;
	    for (Imputation imputation : imputationRepository.findByUser(user)) {
			for (ImputationItem item: imputation.getItems()){
				if (item.getProject().getProduct() == product){
					result += item.getTime();
				}
			}
		}	
		return result;
	}

	@Override
	@Transactional
	public Integer timeByTechnology(Technology technology) {
		Integer result = imputationRepository.timeByTechnology(technology.getId());
		return (result == null) ? 0 : result; 		
	}

	@Override
	@Transactional
	public Integer timeByUser(User user) {

		List<Imputation> imputations = imputationRepository.findByUser(user);

		Integer result = 0;

		for (Imputation imputation : imputations) {
            result += imputation.getItems().stream().mapToInt(ImputationItem::getTime).sum();
        }

		return result;


	}

	@Override
	public List<Map<String, Object>> findByProduct(Long id) {
		return imputationRepository.findByProduct(id);
	}

	@Override
	@Transactional
	public List<Map<String, Object>> findByProject(Long id) {
		return imputationRepository.findByProject(id);
	}

	@Override
	@Transactional
	public Float avgTimeByProduct(Product product) {
		return imputationRepository.avgTimeByProduct(product.getId());
	}

	@Override
	@Transactional
	public Integer avgDurationByProduct(Product product) {
		return imputationRepository.avgDurationByProduct(product.getId());
	}



}
