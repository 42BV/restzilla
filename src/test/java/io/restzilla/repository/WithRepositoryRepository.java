package io.restzilla.repository;

import io.restzilla.model.WithRepository;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithRepositoryRepository extends PagingAndSortingRepository<WithRepository, Long> {
    
}
