package com.arinno.canopus.time.imputation.application;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.time.imputation.domain.Imputation;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.time.imputation.infrastructure.persistence.ImputationRepository;

@Service
public class ImputationServiceImpl implements IImputationService {

    private final ImputationRepository imputationRepository;

	ImputationServiceImpl(ImputationRepository imputationRepository) {
		this.imputationRepository = imputationRepository;
	}

	@Override
	@Transactional(readOnly = true)	
	public List<Imputation> findByUser(User user) {
		return imputationRepository.findByUser(user);
	}

	@Override
	public Optional<Imputation> findByIdAndUser(Long id, User user) {
		return imputationRepository.findByIdAndUser(id, user);
	}	
	
	@Override
	public Optional<Imputation> findByDateAndUser(Date date, User user) {
		return imputationRepository.findByDateAndUser(date, user);
	}	

	@Override
	@Transactional
	public Imputation save(Imputation imputation) {
		return imputationRepository.save(Objects.requireNonNull(imputation, "imputation must not be null"));
	}
	

	// reutilizar la lógica defensiva
	@Override
	@Transactional
	public void deleteByIdAndUser(Long id, User user) {
		// Validamos existencia federada con el usuario activo antes de impactar en el repositorio
		Imputation imputation = findByIdAndUser(id, user)
				.orElseThrow(() -> new ImputationNotFoundException("No se encontró la imputación a eliminar con ID: " + id));
		imputationRepository.delete(imputation);		
	}

	@Override
	@Transactional
	public Integer timeByUser(User user) {

		List<Imputation> imputations = imputationRepository.findByUser(user);

		Integer result = 0;

		for (Imputation imputation : imputations) {
			if (imputation == null || imputation.getItems() == null) {
				continue;
			}
			result += imputation.getItems().stream()
					.filter(Objects::nonNull)
					.mapToInt(item -> item != null ? item.getTime() : 0)
					.sum();
		}

		return result;


	}

}
