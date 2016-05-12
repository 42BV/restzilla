package io.restzilla.repository;

import io.restzilla.model.WithRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithRepositoryRepository extends PagingAndSortingRepository<WithRepository, Long> {

    WithRepository findByActive(boolean active);
    
    List<WithRepository> findAllByActive(boolean active, Sort sort);
    
    Page<WithRepository> findAllByActive(boolean active, Pageable pageable);

}
