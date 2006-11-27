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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class DefaultBuildAgentExtensionManager extends AbstractLogEnabled implements BuildAgentExtentionManager {

    /**
     * @plexus.requirement
     */
    private Map extentions;

    public BuildAgentExtension getBuildAgentExtention(String id) throws NoSuchExtentionException {

        BuildAgentExtension agentExtension = (BuildAgentExtension) extentions.get(id);

        if (agentExtension == null){
            throw new NoSuchExtentionException(id);
        }
        return agentExtension;
    }

    public void postProcess(Map build, HashMap results) {

        for (Iterator iterator = extentions.entrySet().iterator(); iterator.hasNext();) {

            Map.Entry entry = (Map.Entry) iterator.next();

            String name = (String) entry.getKey();

            BuildAgentExtension extension = (BuildAgentExtension) entry.getValue();

            getLogger().info("Executing extention "+name +" post process");

            try {

                extension.postProcess(build, results);

            } catch (Exception e) {

                getLogger().warn("Extention Failed: "+name, e);

            }
        }
    }

    public void preProcess(Map build) {

        for (Iterator iterator = extentions.entrySet().iterator(); iterator.hasNext();) {

            Map.Entry entry = (Map.Entry) iterator.next();

            String name = (String) entry.getKey();

            BuildAgentExtension extension = (BuildAgentExtension) entry.getValue();

            getLogger().info("Executing extention "+name +" pre process");

            try {

                extension.preProcess(build);

            } catch (Exception e) {

                getLogger().warn("Extention Failed: "+name, e);

            }
        }
    }

}
