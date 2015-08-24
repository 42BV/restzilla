package nl.mad.rest.repository;

import nl.mad.rest.model.WithService;

import org.springframework.data.repository.CrudRepository;

public interface WithServiceRepository extends CrudRepository<WithService, Long> {
    
}
