/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Interface for resource, capable of retrieving entities.
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public interface Listable<T> {

    /**
     * Returns all entities, sorted.
     * 
     * @param sort the sort
     * @return all entities
     */
    List<T> findAll(Sort sort);
    
    /**
     * Returns a page of entities.
     * 
     * @param pageable the pageable
     * @return the entities in that page
     */
    Page<T> findAll(Pageable pageable);

}
