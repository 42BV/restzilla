package io.restzilla.web.mapping;

import io.beanmapper.BeanMapper;

import io.restzilla.web.RestInformation;
import org.springframework.data.domain.Persistable;

/**
 * Maps results for our REST operations. 
 *
 * @author Jeroen van Schagen
 * @since Jun 24, 2016
 */
public class BeanMapperAdapter implements Mapper {
    
    private final BeanMapper beanMapper;
    
    private final RestInformation information;
    
    public BeanMapperAdapter(BeanMapper beanMapper, RestInformation information) {
        this.beanMapper = beanMapper;
        this.information = information;
    }

    @Override
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
