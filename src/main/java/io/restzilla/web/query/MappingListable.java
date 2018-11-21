package io.restzilla.web.query;

import java.util.ArrayList;
import java.util.List;

import io.restzilla.web.mapping.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Listable adapter that performs mappings after retrieving the entities.
 *
 * @author Jeroen van Schagen
 * @since Dec 9, 2015
 */
public class MappingListable<T> implements Listable<T>, Finder<T> {
    
    private final Listable<?> delegate;
    
    private final Mapper mapper;
    
    private final Class<T> resultType;
    
    public MappingListable(Listable<?> delegate, Mapper mapper, Class<T> resultType) {
        this.resultType = resultType;
        this.mapper = mapper;
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne() {
        Object entity = ((Finder<?>) delegate).findOne();
        return mapper.map(entity, resultType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        List<?> entities = delegate.findAll(sort);
        return mapList(entities);
    }
    
    private List<T> mapList(List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<T> results = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            T result = mapper.map(entity, resultType);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        Page<?> entities = delegate.findAll(pageable);
        List<T> transformed = mapList(entities.getContent());
        return new PageImpl<T>(transformed, pageable, entities.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return delegate.getEntityClass();
    }

}
