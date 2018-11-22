/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.mapping;

import com.google.common.base.Preconditions;
import io.beanmapper.spring.web.EntityFinder;
import nl._42.restzilla.service.ReadService;

/**
 * Adapts the {@link ReadService} to the {@link EntityFinder} interface.
 *
 * @author Jeroen van Schagen
 * @since Nov 24, 2015
 */
public class ReadServiceEntityFinder implements EntityFinder {
    
    private final ReadService readService;

    /**
     * Create a new {@link ReadServiceEntityFinder} instance.
     * 
     * @param readService the read service
     */
    public ReadServiceEntityFinder(ReadService readService) {
        this.readService = Preconditions.checkNotNull(readService, "Read service is required.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T find(Long id, Class<T> type) {
        return (T) readService.getOne((Class) type, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T findAndDetach(Long id, Class<T> type) {
        return find(id, type);
    }

}
