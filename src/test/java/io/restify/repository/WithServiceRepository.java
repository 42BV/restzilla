package io.restify.repository;

import io.restify.model.WithService;

import org.springframework.data.repository.CrudRepository;

public interface WithServiceRepository extends CrudRepository<WithService, Long> {
    
}
