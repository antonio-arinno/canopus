package com.arinno.canopus.servicies;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;

public interface IImputationService {

    public List<Imputation> findByUser(User user);
	
	public Imputation findByIdAndUser(Long id, User user);
	
	public Imputation findByDateAndUser(Date date, User user);	
	
	public Imputation save(Imputation imputation);
	
	public void deleteByIdAndUser(Long id, User user);

	public Integer timeByProject(Project project);

	public Integer timeByProduct(Product product);

	public Integer timeByProductAndUser(Product product, User user);

	public Integer timeByTechnology(Technology technology);

	public Integer timeByUser(User user);

	public Float avgTimeByProduct(Product product);

	public Integer avgDurationByProduct(Product product);

	public List<Map<String, Object>> findByProduct(Long id);

	public List<Map<String, Object>> findByProject(Long id);


}
