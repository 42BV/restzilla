package nl._42.restzilla.web.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlUtilsTest {
    
    @Test
    public void testStripSlashesEmpty() {
        Assertions.assertEquals("", UrlUtils.stripSlashes(""));
    }
    
    @Test
    public void testStripSlashesBlank() {
        Assertions.assertEquals("", UrlUtils.stripSlashes("  "));
    }
    
    @Test
    public void testStripSlashesSlash() {
        Assertions.assertEquals("", UrlUtils.stripSlashes("/"));
    }
    
    @Test
    public void testStripSlashesText() {
        Assertions.assertEquals("test", UrlUtils.stripSlashes("test"));
    }
    
    @Test
    public void testStripSlashesPreffixed() {
        Assertions.assertEquals("test", UrlUtils.stripSlashes("/test"));
    }

    @Test
    public void testStripSlashesSuffixed() {
        Assertions.assertEquals("test", UrlUtils.stripSlashes("test/"));
    }
    
    @Test
    public void testStripSlashesSeparated() {
        Assertions.assertEquals("a/b", UrlUtils.stripSlashes("a/b"));
    }
    
    @Test
    public void testStripSlashesFull() {
        Assertions.assertEquals("a/b", UrlUtils.stripSlashes("/a/b/"));
    }

}
