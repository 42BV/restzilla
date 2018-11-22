package nl._42.restzilla.repository;

import nl._42.restzilla.model.WithCache;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithCacheRepository extends PagingAndSortingRepository<WithCache, Long> {

}
