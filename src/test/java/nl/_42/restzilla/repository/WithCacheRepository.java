package nl._42.restzilla.repository;

import nl._42.restzilla.model.WithCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithCacheRepository extends JpaRepository<WithCache, Long> {

}
