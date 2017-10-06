package io.restzilla.web;

import io.beanmapper.BeanMapper;
import io.restzilla.RestInformation;

import org.springframework.data.domain.Persistable;

/**
 * Maps results for our REST operations. 
 *
 * @author Jeroen van Schagen
 * @since Jun 24, 2016
 */
public class RestResultMapper {
    
    private final BeanMapper beanMapper;
    
    private final RestInformation information;
    
    public RestResultMapper(BeanMapper beanMapper, RestInformation information) {
        this.beanMapper = beanMapper;
        this.information = information;
    }
    
    /**
     * Enhances our bean mapper with some common non-bean types that can be returned. 
     * 
     * @param <T> the type of result
     * @param source the source
     * @param resultType the result type
     * @return the converted object
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> T map(Object source, Class<T> resultType) {
        if (source == null || Void.class.equals(resultType)) {
            return null;
        } else {
            Class<?> beanClass = beanMapper.getConfiguration().getBeanUnproxy().unproxy(source.getClass());
            if (resultType.isAssignableFrom(beanClass)) {
                return (T) source;
            } else if (information.getIdentifierClass().equals(resultType) && source instanceof Persistable) {
                return (T) ((Persistable) source).getId();
            } else {
                return beanMapper.map(source, resultType);
            }
        }
    }

}
