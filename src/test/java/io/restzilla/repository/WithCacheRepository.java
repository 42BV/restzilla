package io.restzilla.repository;

import io.restzilla.model.WithCache;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithCacheRepository extends PagingAndSortingRepository<WithCache, Long> {

}
