package com.arinno.canopus.servicies;

import java.util.Date;
import java.util.List;

import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.User;

public interface IImputationService {

    public List<Imputation> findByUser(User user);
	
	public Imputation findByIdAndUser(Long id, User user);
	
	public Imputation findByDateAndUser(Date date, User user);	
	
	public Imputation save(Imputation imputation);
	
	public void deleteByIdAndUser(Long id, User user);

}
