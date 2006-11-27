package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DirectoryMonitorTest extends TestCase {

    public void testScanDirectory() throws Exception {
        Status status = new Status();

        File dir = File.createTempFile("abc","xyz").getParentFile();
        dir = new File(dir, "dirscan"+(System.currentTimeMillis()%100000));
        assertTrue("mkdir "+dir.getAbsolutePath(), dir.mkdir());
        dir.deleteOnExit();

        DirectoryMonitor monitor = new DirectoryMonitor(dir, status, 0, new TestLogger());
        monitor.initialize();

        assertNull("file",status.file);

        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Discovered
        File aaa = new File(dir, "aaa");
        writeToFile(aaa);
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // New
        monitor.scanDirectory();
        assertEquals("state",Status.ADDED, status.state);
        assertEquals("file",aaa.getName(), status.file.getName());
        status.reset();

        // Nothing different
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Updated (Write, scan, scan)
        writeToFile(aaa);
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        monitor.scanDirectory();
        assertEquals("state",Status.UPDATED, status.state);
        assertEquals("file",aaa.getName(), status.file.getName());
        status.reset();

        // Nothing different
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Nothing different
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Remove
        assertTrue("delete", aaa.delete());
        assertTrue("deleted", !aaa.exists());
        monitor.scanDirectory();
        assertEquals("state",Status.REMOVED, status.state);
        assertEquals("file",aaa.getName(), status.file.getName());
        status.reset();

        // Nothing different
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Nothing different
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // Added again
        writeToFile(aaa);
        monitor.scanDirectory();
        assertNull("state",status.state);
        assertNull("file",status.file);

        // New
        monitor.scanDirectory();
        assertEquals("state",Status.ADDED, status.state);
        assertEquals("file",aaa.getName(), status.file.getName());
        status.reset();
    }

    private void writeToFile(File aaa) throws IOException {
        FileOutputStream out = new FileOutputStream(aaa, true);
        out.write(1);
        out.write(1);
        out.write(1);
        out.close();
    }


    public static class Status implements DirectoryMonitor.Listener {

        public static final String REMOVED = "REMOVED";
        public static final String UPDATED = "UPDATED";
        public static final String ADDED = "ADDED";

        public File file;
        public String state;

        public boolean fileAdded(File file) {
            state = ADDED;
            this.file = file;
            return true;
        }

        public boolean fileRemoved(File file) {
            state = REMOVED;
            this.file = file;
            return true;
        }

        public void fileUpdated(File file) {
            state = UPDATED;
            this.file = file;
        }

        public void reset() {
            state = null;
            file = null;
        }
    }

}