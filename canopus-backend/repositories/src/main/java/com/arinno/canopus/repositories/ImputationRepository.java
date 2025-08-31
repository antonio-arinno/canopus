package com.arinno.canopus.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.User;

public interface ImputationRepository extends CrudRepository<Imputation, Long> {

    public List<Imputation> findByUser(User user);
	
	public Imputation findByIdAndUser(Long id, User user);
	
	public Imputation findByDateAndUser(Date date, User user);
	
	public void deleteByIdAndUser(Long id, User user);

	@Query("select sum(time) from ImputationItem where project.id = ?1")
	public Integer timeByProject(Long id);

	@Query("select sum(time) from Product product left join Project project on product.id = project.product.id left join ImputationItem item on project.id = item.project.id where product.id = ?1")
    public Integer timeByProduct(Long id);
 																                                           
	@Query
	("select sum(time) from Technology technology left join Product product on technology.id = product.technology.id left join Project project on product.id = project.product.id left join ImputationItem item on project.id = item.project.id where technology.id = ?1")
    public Integer timeByTechnology(Long id);

	@NativeQuery
	("select projects.name as projectName, count(distinct users.id) as countContributors, sum(imputations_items.time) as time from imputations "+
	 "left join users on imputations.user_id = users.id "+
	 "left join imputations_items on imputations.id = imputations_items.imputation_id "+
	 "left join projects on imputations_items.project_id = projects.id "+
	 "left join products on projects.product_id = products.id "+
	 "where products.id = ?1 "+
	 "group by project_id, product_id")
	public List<Map<String, Object>> findByProduct(Long id);

	@NativeQuery
	("select name, sum(time) as time from imputations "+
	 "left join users on imputations.user_id = users.id "+
	 "left join imputations_items on imputations.id = imputations_items.imputation_id "+
	 "where project_id = ?1 "+
	 "group by user_id")
	public List<Map<String, Object>> findByProject(Long id);

	@NativeQuery
	("select sum(time) / count(distinct project_id) from projects "+
	 "left join imputations_items on projects.id = imputations_items.project_id "+
	 "where product_id = ?1 "+
	 "and not isnull(date_pro)")
	public Float avgTimeByProduct(Long id);	

	@NativeQuery
	("select sum(datediff (date_pro, date_dev)) / count(*) from projects where product_id = ?1 and  not isnull(date_pro)")
	public Integer avgDurationByProduct(Long id);	

}
