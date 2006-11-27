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

import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;
import javax.jms.Connection;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class BuildResultsContinuumAgent extends AbstractContinuumBuildAgent {

    /**
     * @plexus.requirement
     */
    private BuildResultsExtensionManager extentionManager;

    /**
     * @plexus.configuration
     */
    private String buildResultsTopic;


    public void run() {
        try {
            getLogger().info("Results Agent starting.");
            getLogger().debug("buildResultsTopic " + buildResultsTopic);

            while (isRunning()) {
                // Create a Session
                Client client = getClient();

                Session session = client.getSession();

                MessageConsumer resultsConsumer = createConsumer(session, buildResultsTopic);

                try {
                    consumeMessages(client, resultsConsumer);
                } catch (JMSException e) {
                    getLogger().error("Agent recieved JMS Exception. ("+e.getMessage()+")");
                }
            }

        } catch (Exception e) {
            getLogger().error("Agent failed.", e);
        }
    }

    private void consumeMessages(Client client, MessageConsumer resultsConsumer) throws JMSException {
        while (client.isConnected() && isRunning()) {
            // Wait for a message
            Message message = resultsConsumer.receive(1000);

            if (message == null){

                continue;

            } else if (message instanceof ObjectMessage) {

                try {
                    Connection connection = client.getConnection();
                    getLogger().info("Message Received "+ message.getJMSMessageID() +" on "+ connection.getClientID()+":"+buildResultsTopic);

                    ObjectMessage objectMessage = (ObjectMessage) message;

                    Map context = getMap(objectMessage, message);

                    execute(context);

                    getLogger().info("Finished processing "+ message.getJMSMessageID());

                } catch (Exception e) {
                    getLogger().error("Failed Processing message "+message.getJMSMessageID());
                }

            } else {
                getLogger().warn("Agent received incorrect message type: "+message.getClass().getName());
            }
        }
    }

    public void execute(Map map) throws Exception {
        extentionManager.execute(map);
    }


}
