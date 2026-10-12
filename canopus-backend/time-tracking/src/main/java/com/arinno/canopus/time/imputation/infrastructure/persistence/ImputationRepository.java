package com.arinno.canopus.time.imputation.infrastructure.persistence;




import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.time.imputation.domain.Imputation;
import com.arinno.canopus.organization.user.domain.User;

public interface ImputationRepository extends CrudRepository<Imputation, Long> {

    public List<Imputation> findByUser(User user);
	
	public Optional<Imputation> findByIdAndUser(Long id, User user);
	
	public Optional<Imputation> findByDateAndUser(Date date, User user);
	
	public void deleteByIdAndUser(Long id, User user);

}
