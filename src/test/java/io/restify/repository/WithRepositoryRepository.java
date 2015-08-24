package io.restify.repository;

import io.restify.model.WithRepository;

import org.springframework.data.repository.CrudRepository;

public interface WithRepositoryRepository extends CrudRepository<WithRepository, Long> {
    
}
