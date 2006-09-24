/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.gbuild.extension.requeue;

import org.apache.geronimo.gbuild.agent.AbstractContinuumAgentAction;
import org.apache.geronimo.gbuild.agent.BuildAgentExtension;
import org.apache.geronimo.gbuild.agent.BuildResultsExtension;
import org.apache.geronimo.gbuild.agent.ClientManager;
import org.apache.geronimo.gbuild.agent.BuildActivityNotifier;
import org.apache.geronimo.gbuild.agent.ContinuumBuildAgent;
import org.apache.geronimo.gbuild.agent.StringTemplate;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.MessageProducer;
import javax.jms.DeliveryMode;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Decided to put the BuildAgentExtension and BuildResultsExtension both in this class
 * due to the symmetrical nature of them.  They largely complete the same task, simply
 * the task is started on one machine and completed on another.  Client and Server if
 * you want to think of it that way.
 *
 * @version $Rev$ $Date$
 */
public class RequeueFailedBuilds extends AbstractLogEnabled {

    public static final String PREFIX = RequeueFailedBuilds.class.getName() + "@";
    public static final String BUILD_TASK = PREFIX + "build-task";
    public static final String HOSTS_LIST = PREFIX + "hosts-list";

    public int getExitCode(Map context) {
        try {
            ContinuumStore store = AbstractContinuumAgentAction.getContinuumStore(context);

            int projectId = AbstractContinuumAction.getProjectId(context);

            Project project = store.getProject(projectId);

            BuildResult buildResult = store.getBuildResult(project.getLatestBuildId());

            return buildResult.getExitCode();
        } catch (ContinuumStoreException e) {
            getLogger().error("Unable to use continuum store.", e);
            return -1;
        }
    }

    public static class AgentExtension extends RequeueFailedBuilds implements BuildAgentExtension {
        public static void main(String[] args) {
            String name = AgentExtension.class.getName();
            System.out.println("name = " + name);
        }
        public void preProcess(Map build) {
        }

        public void postProcess(Map build, Map results) {
            if (getExitCode(results) != 0) {
                results.put(BUILD_TASK, new HashMap(build));
            }
        }
    }

    public static class ResultsExtension extends RequeueFailedBuilds implements BuildResultsExtension {
        /**
         * @plexus.requirement
         */
        private ClientManager clientManager;

        /**
         * @plexus.requirement
         */
        private BuildActivityNotifier notifier;

        /**
         * @plexus.configuration
         */
        private String buildTaskQueue;

        /**
         * @plexus.configuration
         */
        private int maxRequeue;

        private StringTemplate failedLogMessage = new StringTemplate("Unable to requeue failed build: [{host-name}:{host-address}] {project.name}-{project.version} - {build.name} {build.id}");

        private StringTemplate failedNotification = new StringTemplate("Unable to requeue failed build from [{host-name}:{host-address}] - {os.name} {os.version}");

        private StringTemplate requeuedMessage = new StringTemplate("Requeued: Agent info [{host-name}:{host-address}] - {os.name} {os.version}");

        private StringTemplate requeueNotPossible = new StringTemplate("Discarding - Requeue Not Possible: Agent info [{host-name}:{host-address}] - {os.name} {os.version}");

        private StringTemplate maxRetriesExceeded = new StringTemplate("Discarding - Max Retries Exceeded: Agent info [{host-name}:{host-address}] - {os.name} {os.version}");

        public void execute(Map results) throws Exception {
            if (getExitCode(results) == 0) {
                return;
            }

            HashMap buildTask = (HashMap) results.get(BUILD_TASK);

            if (buildTask == null) {

                notifier.sendNotification(results, requeueNotPossible.apply(results));

                return;
            }

            ArrayList hostsList = (ArrayList) buildTask.get(HOSTS_LIST);

            if (hostsList == null){
                hostsList = new ArrayList();
            }

            String failedOnHost = getHostName(results);

            hostsList.add(failedOnHost);

            if (hostsList.size() >= maxRequeue) {

                notifier.sendNotification(results, maxRetriesExceeded.apply(results));

                return;
            }

            try{
                Session session = clientManager.getClient().getSession();

                Queue buildQueue = session.createQueue(buildTaskQueue);

                MessageProducer producer = session.createProducer(buildQueue);

                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                ObjectMessage message = session.createObjectMessage(buildTask);

                for (int i = 0; i < hostsList.size(); i++) {

                    String hostName = (String) hostsList.get(i);

                    message.setStringProperty(hostName, "exclude");
                }

                producer.send(message);

                notifier.sendNotification(buildTask, requeuedMessage.apply(results));

            } catch (JMSException e) {

                getLogger().error(failedLogMessage.apply(results));

            } catch (Exception e) {

                notifier.sendNotification(results, failedNotification.apply(results));
            }
        }

        private String getHostName(Map results) {
            return (String) results.get(ContinuumBuildAgent.KEY_HOST_NAME);
        }
    }
}
