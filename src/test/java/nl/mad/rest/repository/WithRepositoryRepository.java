package nl.mad.rest.repository;

import nl.mad.rest.model.WithRepository;

import org.springframework.data.repository.CrudRepository;

public interface WithRepositoryRepository extends CrudRepository<WithRepository, Long> {
    
}
