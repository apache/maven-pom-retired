/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.JMSException;
import java.util.Enumeration;
import java.io.File;
import java.io.FilenameFilter;

/**
 * @version $Rev$ $Date$
 */
public class BuildQueueReloader extends AbstractLogEnabled implements Startable, Runnable {

    /**
     * @plexus.requirement
     */
    private ClientManager clientManager;

    /**
     * @plexus.configuration
     */
    private String buildTaskQueue;

    /**
     * @plexus.configuration
     */
    private String tasksDirectory;

    /**
     * @plexus.configuration
     */
    private int pollInterval;


    private boolean running;

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void start() throws StartingException {
        setRunning(true);
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() throws StoppingException {
        setRunning(false);
    }

    public void run() {
        while (isRunning()) {
            checkTasksQueue();
            try {
                Thread.sleep(pollInterval*60000);
            } catch (InterruptedException dontCare) {}
        }
    }

    private void checkTasksQueue() {
        if (isEmpty()){
            getLogger().info(buildTaskQueue+" empty");
            long now = System.currentTimeMillis();

            File dir = new File(tasksDirectory);
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.getName().endsWith("properties")) {
                    getLogger().info("Touching file "+file.getName());

                    file.setLastModified(now);
                }
            }
        }
    }

    private boolean isEmpty() {
        try {
            Client client = clientManager.getClient();

            Session session = client.getSession();
            Queue queue = session.createQueue(buildTaskQueue);
            QueueBrowser browser = session.createBrowser(queue);

            Enumeration enumeration = browser.getEnumeration();
            boolean empty = !enumeration.hasMoreElements();
            return empty;
        } catch (JMSException e) {
            throw (IllegalStateException)new IllegalStateException("JMS Failure").initCause(e);
        }
    }
}
