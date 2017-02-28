package io.restzilla.util;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilsTest {
    
    @Test
    public void testStripSlashesEmpty() {
        Assert.assertEquals("", UrlUtils.stripSlashes(""));
    }
    
    @Test
    public void testStripSlashesBlank() {
        Assert.assertEquals("", UrlUtils.stripSlashes("  "));
    }
    
    @Test
    public void testStripSlashesSlash() {
        Assert.assertEquals("", UrlUtils.stripSlashes("/"));
    }
    
    @Test
    public void testStripSlashesText() {
        Assert.assertEquals("test", UrlUtils.stripSlashes("test"));
    }
    
    @Test
    public void testStripSlashesPreffixed() {
        Assert.assertEquals("test", UrlUtils.stripSlashes("/test"));
    }

    @Test
    public void testStripSlashesSuffixed() {
        Assert.assertEquals("test", UrlUtils.stripSlashes("test/"));
    }
    
    @Test
    public void testStripSlashesSeparated() {
        Assert.assertEquals("a/b", UrlUtils.stripSlashes("a/b"));
    }
    
    @Test
    public void testStripSlashesFull() {
        Assert.assertEquals("a/b", UrlUtils.stripSlashes("/a/b/"));
    }

}
