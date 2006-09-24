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
import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class PingMonitorAgent extends AbstractContinuumBuildAgent {

    private String subject = "BUILD.PING";

    public void execute(Map context) throws Exception {
    }

    public void run() {
        try {
            getLogger().info("Ping Agent starting.");

            while (isRunning()){
                Client client = getClient();

                Session session = client.getSession();

                MessageConsumer consumer = createConsumer(session, subject);

                try {
                    while (client.isConnected() && isRunning()) {
                        Message message = consumer.receive(1000);

                        if (message == null){
                            continue;
                        } else if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            getLogger().debug("Ping "+ message.getJMSMessageID() +" : "+ textMessage.getText());
                        } else {
                            getLogger().warn("Agent received incorrect message type: "+message.getClass().getName());
                        }
                    }
                } catch (JMSException e) {
                    getLogger().info("Burp. "+e.getMessage());
                }
            }
        } catch (Exception e) {
            getLogger().error("Agent failed.", e);
        }

    }
}
