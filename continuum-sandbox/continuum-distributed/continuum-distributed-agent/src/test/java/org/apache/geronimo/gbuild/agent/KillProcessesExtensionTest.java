package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.*;
import org.apache.geronimo.gbuild.agent.KillProcessesExtension;

import java.util.List;

public class KillProcessesExtensionTest extends TestCase {

    public void testNothing() throws Exception {

    }

    public void notReliable_testFindProcessIds() throws Exception {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        if (isWindows){
            return;
        }
        
        Runtime runtime = Runtime.getRuntime();

        runtime.exec("sleep 43210");
        runtime.exec("sleep 43210");
        runtime.exec("sleep 43210");

        List pids = KillProcessesExtension.findProcessIds(".*sleep 43210.*");

        assertEquals("processes started: pids.size", 3, pids.size());
        KillProcessesExtension.killProcesses(pids);

        pids = KillProcessesExtension.findProcessIds(".*sleep 43210.*");
        assertEquals("processes killed: pids.size", 0, pids.size());
    }
}