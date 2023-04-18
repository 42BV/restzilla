package nl._42.restzilla.repository;

import nl._42.restzilla.model.WithRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithRepositoryRepository extends JpaRepository<WithRepository, Long> {

    WithRepository findByActive(boolean active);
    
    List<WithRepository> findAllByActive(boolean active, Sort sort);
    
    Page<WithRepository> findAllByActive(boolean active, Pageable pageable);

}
