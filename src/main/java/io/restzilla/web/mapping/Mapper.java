package io.restzilla.web.mapping;

public interface Mapper {

    /**
     * Enhances our bean mapper with some common non-bean types that can be returned.
     *
     * @param <T> the type of result
     * @param source the source
     * @param resultType the result type
     * @return the converted object
     */
    <T> T map(Object source, Class<T> resultType);

}