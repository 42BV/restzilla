/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.repository;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the {@link CrudRepository} that it runs in.
 *
 * @author Jeroen van Schagen
 * @since Nov 11, 2015
 */
public interface RepositoryAware<T extends Persistable<ID>, ID extends Serializable> {

    /**
     * Set the repository that is used by this bean.
     * @param repository the repository instance
     */
    void setRepository(PagingAndSortingRepository<T, ID> repository);
    
}
