package io.restify.repository;

import io.restify.model.WithService;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithServiceRepository extends PagingAndSortingRepository<WithService, Long> {
    
}
