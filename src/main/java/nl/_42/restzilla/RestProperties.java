package nl._42.restzilla;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;

public class RestProperties {

    public static final String DEFAULT_PAGE_NAME = "restzilla.default-page";
    public static final String DEFAULT_SIZE_NAME = "restzilla.default-size";
    public static final String MAX_SIZE_NAME = "restzilla.max-size";
    public static final String DEFAULT_SORT_NAME = "restzilla.default-sort";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 250;

    private final int defaultPage;
    private final int defaultSize;
    private final int maxPageSize;
    private final Sort defaultSort;

    public RestProperties() {
        this.defaultPage = DEFAULT_PAGE;
        this.defaultSize = DEFAULT_SIZE;
        this.maxPageSize = MAX_SIZE;
        this.defaultSort = Sort.unsorted();
    }

    public RestProperties(Environment environment) {
        this.defaultPage = environment.getProperty(DEFAULT_PAGE_NAME, int.class, DEFAULT_PAGE);
        this.defaultSize = environment.getProperty(DEFAULT_SIZE_NAME, int.class, DEFAULT_SIZE);
        this.maxPageSize = environment.getProperty(MAX_SIZE_NAME, int.class, MAX_SIZE);

        String property = environment.getProperty(DEFAULT_SORT_NAME);
        if (StringUtils.isNotBlank(property)) {
            this.defaultSort = Sort.by(property);
        } else {
            this.defaultSort = Sort.unsorted();
        }
    }

    public int getDefaultPage() {
        return defaultPage;
    }

    public int getDefaultSize() {
        return defaultSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public Sort getDefaultSort() {
        return defaultSort;
    }

    public int getPage(int value) {
        return defaultPage > 0 ? value - 1 : value;
    }

}
