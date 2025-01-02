package com.arinno.canopus.servicies;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.entities.Imputation;
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

}
