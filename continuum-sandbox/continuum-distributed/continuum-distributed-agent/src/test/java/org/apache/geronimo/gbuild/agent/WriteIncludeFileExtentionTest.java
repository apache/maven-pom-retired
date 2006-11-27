package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

public class WriteIncludeFileExtentionTest extends TestCase {

    public void testExecute() throws Exception {
        WriteIncludeFileExtension extention = new WriteIncludeFileExtension("^include.*", "{foo}/{name}.properties", "target", "yyyy");
        extention.enableLogging(new TestLogger("include-writer"));
        extention.start();

        HashMap map = new HashMap();
        map.put("include.hello", "id=abc");
        map.put("foo", "include-test");
        map.put("name", "one");
        extention.execute(map);

        File file = new File("target/include-test/one.properties");
        assertTrue("file.exists()", file.exists());
        assertEquals("file.size", 6, file.length());

        FileInputStream in = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(in);
        in.close();
        String property = properties.getProperty("id");
        assertNotNull("property", property);
        assertEquals("property", "abc", property);

        file.delete();
    }
}