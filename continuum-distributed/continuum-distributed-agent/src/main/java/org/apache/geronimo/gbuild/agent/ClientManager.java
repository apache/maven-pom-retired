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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class ClientManager extends AbstractLogEnabled implements ExceptionListener, Startable {

    /**
     * @plexus.configuration
     */
    private String brokerUrl;

    /**
     * @plexus.configuration
     */
    private int pingInterval;

    /**
     * @plexus.configuration
     */
    private int reconnectAttempts;

    /**
     * @plexus.configuration
     */
    private int reconnectDelay;

    /**
     * @plexus.configuration
     */
    private String reconnectDelayUnit = "seconds";

    private Map timeUnits = new HashMap();

    private Client client;

    public ClientManager() {
        int unit = 1000;
        timeUnits.put("seconds", new Integer(unit));
        timeUnits.put("sec", new Integer(unit));
        timeUnits.put("s", new Integer(unit));

        unit *= 60;
        timeUnits.put("minutes", new Integer(unit));
        timeUnits.put("min", new Integer(unit));
        timeUnits.put("m", new Integer(unit));

        unit *= 60;
        timeUnits.put("hours", new Integer(unit));
        timeUnits.put("hour", new Integer(unit));
        timeUnits.put("h", new Integer(unit));

        unit *= 24;
        timeUnits.put("days", new Integer(unit));
        timeUnits.put("day", new Integer(unit));
        timeUnits.put("d", new Integer(unit));
    }

    public ClientManager(String brokerUrl, int pingInterval, int reconnectAttempts, int reconnectDelay) {
        this();
        this.brokerUrl = brokerUrl;
        this.pingInterval = pingInterval;
        this.reconnectAttempts = reconnectAttempts;
        this.reconnectDelay = reconnectDelay;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public synchronized void start() throws StartingException {
        Integer unit = (Integer) timeUnits.get(reconnectDelayUnit);
        if (unit == null){
            unit = new Integer(1000); //seconds
        }
        try {
            setClient(new Client(brokerUrl, this, getLogger(), reconnectDelay * unit.intValue(), reconnectAttempts, pingInterval));
        } catch (Throwable e) {
            getLogger().error("Could not create connection to: " + brokerUrl, e);
            throw new StartingException("Could not create connection to: " + brokerUrl);
        }
        getLogger().info("Client connected: " + brokerUrl);
    }

    public synchronized void stop() throws StoppingException {
        try {
            getClient().close();
        } catch (JMSException e) {
            getLogger().error("Could not close connection to: " + brokerUrl, e);
            throw new StoppingException("Could not close connection to: " + brokerUrl);
        }
    }

    public void onException(JMSException ex) {
        getLogger().fatalError("JMS Exception occured.  Attempting reconnect.", ex);
        try {
            reconnect();
        } catch (JMSException e) {
            getLogger().error("Reconnect failed.", e);
        }
    }

    private synchronized void reconnect() throws JMSException {
        this.client = client.reconnect();
    }

    public synchronized Client getClient() {
        return client;
    }

    private synchronized void setClient(Client client) {
        this.client = client;
    }
}
