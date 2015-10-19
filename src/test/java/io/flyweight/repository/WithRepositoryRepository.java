package io.flyweight.repository;

import io.flyweight.model.WithRepository;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithRepositoryRepository extends PagingAndSortingRepository<WithRepository, Long> {
    
}
