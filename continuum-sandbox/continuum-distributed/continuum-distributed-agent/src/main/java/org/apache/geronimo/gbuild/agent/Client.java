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

import org.codehaus.plexus.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;

import javax.jms.ExceptionListener;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.DeliveryMode;
import javax.jms.TextMessage;

/**
 * @version $Rev$ $Date$
 */
public class Client implements ExceptionListener {

    private final String brokerUrl;
    private final Connection connection;
    private final Session session;
    private final ExceptionListener listener;
    private final Logger logger;
    private boolean connected = true;
    private final Ping ping;
    private final int delay;
    private final int maxTries;

    private Client(Client old, Connection connection, Session session) throws JMSException {
        this.brokerUrl = old.brokerUrl;
        this.delay = old.delay;
        this.maxTries = old.maxTries;
        this.listener = old.listener;
        this.logger = old.logger;
        this.connection = connection;
        this.session = session;

        this.ping = new Ping(session, getLogger(), old.ping.getInterval());
        ping.start();
     }

    public Client(String brokerUrl, ExceptionListener listener, Logger logger, int reconnectDelay, int reconnectTries, int pingInterval) throws JMSException {
        this.brokerUrl = brokerUrl;
        connection = createConnection(brokerUrl);
        connection.setExceptionListener(this);
        connection.start();
        this.listener = listener;
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.logger = logger;
        this.delay = reconnectDelay;
        this.maxTries = reconnectTries;
        this.ping = new Ping(session, logger, pingInterval);
        ping.start();
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    private Logger getLogger() {
        return logger;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public MessageConsumer createQueueConsumer(String subject) throws JMSException {
        Queue queue = session.createQueue(subject);
        return session.createConsumer(queue);
    }

    public MessageConsumer createQueueConsumer(String subject, String selector) throws JMSException {
        Queue queue = session.createQueue(subject);
        return session.createConsumer(queue, selector);
    }

    public MessageConsumer createTopicConsumer(String subject) throws JMSException {
        Topic topic = session.createTopic(subject);
        return session.createConsumer(topic);
    }

    public MessageProducer createTopicProducer(String subject) throws JMSException {
        Topic topic = session.createTopic(subject);
        MessageProducer producer = session.createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    public synchronized Client reconnect() throws JMSException {
        failed();

        Connection connection = connect();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        return new Client(this, connection, session);
    }

    public synchronized void close() throws JMSException {
        ping.stop();
        session.close();
        connection.close();
    }

    private Connection connect() throws JMSException {
        return connect(maxTries);
    }

    private Connection connect(int tries) throws JMSException {

        try {
            Connection connection = createConnection(brokerUrl);
            connection.setExceptionListener(this);
            connection.start();
            getLogger().info("Client reconnect successful.");
            return connection;
        } catch (JMSException e) {
            if (tries <= 0) {
                getLogger().info("Client reconnect failed.  Giving up.", e);
                throw e;
            } else {
                try {
                    getLogger().info("Client reconnect failed.  Trying again in "+delay+" milliseconds. ("+ e.getMessage()+")");
                    Thread.sleep(delay);
                } catch (InterruptedException dontCare) {
                }
                return connect(--tries);
            }
        }
    }

    private Connection createConnection(String brokerUrl) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        ActiveMQPrefetchPolicy prefetchPolicy = connectionFactory.getPrefetchPolicy();
        prefetchPolicy.setQueuePrefetch(1);
        return connectionFactory.createConnection();
    }

    /**
     * Marks this client as failed and returns its previous state
     * @return false if the client was not previously in a failed state
     */
    private synchronized boolean failed() {
        boolean failed = !connected;
        connected = false;
        return failed;
    }

    public void onException(JMSException jmsException) {
        getLogger().info("JMSException "+this.hashCode());
        this.listener.onException(jmsException);
    }

    public static class Ping implements Runnable {
        private boolean run;
        private final Session session;
        private final MessageProducer producer;
        private final Logger logger;
        private final int interval;

        public Ping(Session session, Logger logger, int interval) throws JMSException {
            this.logger = logger;
            this.run = true;
            this.interval = interval;

            this.session = session;
            Topic topic = session.createTopic("BUILD.PING");
            this.producer = session.createProducer(topic);
        }

        public int getInterval() {
            return interval;
        }

        public Logger getLogger() {
            return logger;
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void stop(){
            setRun(false);
        }

        public synchronized boolean isRunning() {
            return run;
        }

        public synchronized void setRun(boolean run) {
            this.run = run;
        }

        public void run() {
            while (isRunning()){
                try {
                    ping();
                } catch (JMSException e) {
                    getLogger().warn("Ping thread killed ("+e.getMessage()+")");
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
        }

        private void ping() throws JMSException {
            TextMessage message = session.createTextMessage(Long.toString(System.currentTimeMillis()));
            producer.send(message);
        }
    }

}
