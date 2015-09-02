package io.restify.repository;

import io.restify.model.WithRepository;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithRepositoryRepository extends PagingAndSortingRepository<WithRepository, Long> {
    
}
