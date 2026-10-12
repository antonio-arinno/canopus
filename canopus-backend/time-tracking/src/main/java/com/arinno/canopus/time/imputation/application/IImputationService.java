package com.arinno.canopus.time.imputation.application;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.arinno.canopus.time.imputation.domain.Imputation;
import com.arinno.canopus.organization.user.domain.User;

public interface IImputationService {

    public List<Imputation> findByUser(User user);
	
	public Optional<Imputation> findByIdAndUser(Long id, User user);
	
	public Optional<Imputation> findByDateAndUser(Date date, User user);	
	
	public Imputation save(Imputation imputation);
	
	public void deleteByIdAndUser(Long id, User user);

	public Integer timeByUser(User user);
}
