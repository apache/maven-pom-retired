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

import javax.jms.ObjectMessage;
import javax.jms.MessageProducer;
import javax.jms.JMSException;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @version $Rev$ $Date$
 */
public class BuildActivityNotifier extends AbstractLogEnabled {

    /**
     * @plexus.requirement
     */
    private ClientManager clientManager;

    /**
     * @plexus.configuration
     */
    private String buildActivityTopic;

    
    public void sendNotification(Map build, String notification) {
        HashMap map = new HashMap();

        map.putAll(build);

        ContinuumBuildAgent.setSystemProperty(map, ContinuumBuildAgent.KEY_OS_VERSION);

        ContinuumBuildAgent.setSystemProperty(map, ContinuumBuildAgent.KEY_OS_NAME);

        ContinuumBuildAgent.setSystemProperty(map, ContinuumBuildAgent.KEY_JAVA_VERSION);

        ContinuumBuildAgent.setSystemProperty(map, ContinuumBuildAgent.KEY_JAVA_VENDOR);

        try {
            InetAddress localHost = InetAddress.getLocalHost();

            map.put(ContinuumBuildAgent.KEY_HOST_NAME, localHost.getHostName());

            map.put(ContinuumBuildAgent.KEY_HOST_ADDRESS, localHost.getHostAddress());
        } catch (UnknownHostException e) {
            getLogger().warn("Unable to determine local host information", e);
        }

        map.put("notification", notification);

        try {
            Client client = clientManager.getClient();

            ObjectMessage message = client.getSession().createObjectMessage(map);

            MessageProducer producer = client.createTopicProducer(buildActivityTopic);

            producer.send(message);
        } catch (JMSException e1) {
            getLogger().error("Send failed.", e1);
        }
    }

}
