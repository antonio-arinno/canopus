package com.arinno.canopus.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.User;

public interface ImputationRepository extends CrudRepository<Imputation, Long> {

    public List<Imputation> findByUser(User user);
	
	public Imputation findByIdAndUser(Long id, User user);
	
	public Imputation findByDateAndUser(Date date, User user);
	
	public void deleteByIdAndUser(Long id, User user);
/*
	@Query("delete from ImputationItem item where item.imputation.id = ?1")
	public void deleteImputationItems(Long imputation_id);
 */
}
