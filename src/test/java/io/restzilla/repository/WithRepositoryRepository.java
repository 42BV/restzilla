package io.restzilla.repository;

import io.restzilla.model.WithRepository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithRepositoryRepository extends PagingAndSortingRepository<WithRepository, Long> {

    List<WithRepository> findAllByActive(boolean active);

}
