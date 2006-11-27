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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.DeliveryMode;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractContinuumBuildAgent extends AbstractContinuumAgentAction implements BuildAgent, Startable {

    private boolean run;

    /**
     * @plexus.requirement
     */
    private ClientManager clientManager;

    public synchronized void start() throws StartingException {
        run = true;
        Thread agentThread = new Thread(this);
        agentThread.setDaemon(false);
        agentThread.start();
    }

    public synchronized void stop() throws StoppingException {
        run = false;
    }

    public synchronized boolean isRunning() {
        return run;
    }

    protected static MessageConsumer createQueueConsumer(Session session, String subject) throws JMSException {
        Queue queue = session.createQueue(subject);

        return session.createConsumer(queue);
    }

    protected static MessageProducer createTopicProducer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);

        MessageProducer producer = session.createProducer(topic);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return producer;
    }

    protected static MessageConsumer createConsumer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);
        return session.createConsumer(topic);
    }

    public static Map getMap(ObjectMessage objectMessage, Message message) throws JMSException, BuildAgentException {
        try {
            return (Map) objectMessage.getObject();
        } catch (Exception e) {
            throw new BuildAgentException("Message.getObject failed on "+ message.getJMSMessageID(), e);
        }
    }

    public synchronized void setRun(boolean run) {
        this.run = run;
    }

    public synchronized Client getClient() {
        return clientManager.getClient();
    }

}
