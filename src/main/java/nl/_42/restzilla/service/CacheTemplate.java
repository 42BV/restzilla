package nl._42.restzilla.service;

import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;

import java.util.function.Supplier;

public class CacheTemplate {

    private static final Cache EMPTY = new NoOpCache("empty");

    private final Cache cache;

    public CacheTemplate() {
        this(EMPTY);
    }

    public CacheTemplate(Cache cache) {
        this.cache = cache;
    }

    @SuppressWarnings("unchecked")
    // Using putIfAbsent causes a recursive update in certain cases
    public <R> R lookup(final String key, final Supplier<R> retriever) {
        Cache.ValueWrapper cached = cache.get(key);
        if (cached == null) {
            R result = retriever.get();
            cache.put(key, result);
            return result;
        } else {
            return (R) cached.get();
        }
    }

    public Cache getCache() {
        return cache;
    }

    public void clear() {
        cache.clear();
    }

}
