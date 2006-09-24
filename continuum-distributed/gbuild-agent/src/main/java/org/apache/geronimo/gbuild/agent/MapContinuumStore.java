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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class MapContinuumStore extends MockContinuumStore implements Serializable {

    private Map store = new HashMap();

    public Project getProjectWithBuildDetails(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getProject(projectId);
    }

    public Project getProject(int projectId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return (Project) getObjectById(Project.class, projectId);
    }

    public void updateProject(Project project) throws ContinuumStoreException {
        updateObject(project, project.getId());
    }

    public BuildResult getBuildResult(int buildId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return (BuildResult) getObjectById(BuildResult.class, buildId);
    }

    public void updateBuildResult(BuildResult build) throws ContinuumStoreException {
        updateObject(build, build.getId());
    }

    public void addBuildResult(Project project, BuildResult build) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        updateObject(build, build.getId());

        project.setLatestBuildId(build.getId());

        project.setState(build.getState());

        project.addBuildResult(build);
    }

    public BuildDefinition getBuildDefinition(int buildDefinitionId)
            throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return (BuildDefinition) getObjectById(BuildDefinition.class, buildDefinitionId);
    }

    public BuildDefinition storeBuildDefinition(BuildDefinition buildDefinition)
            throws ContinuumStoreException {
        updateObject(buildDefinition, buildDefinition.getId());

        return buildDefinition;
    }

    public SystemConfiguration getSystemConfiguration() throws ContinuumStoreException {
        return (SystemConfiguration) getMap(SystemConfiguration.class).get(new Integer(0));
    }

    public SystemConfiguration addSystemConfiguration(SystemConfiguration systemConf) {
        updateObject(systemConf, 0);
        return systemConf;
    }

    private void updateObject(Object object, int id) {

        Map map = getMap(object.getClass());

        map.put(new Integer(id), object);
    }

    private Object getObjectById(Class clazz, int id)
            throws ContinuumStoreException, ContinuumObjectNotFoundException {
        Map map = getMap(clazz);

        Object objectId = new Integer(id);

        Object object = map.get(objectId);

        if (object == null) {

            throw new ContinuumObjectNotFoundException(clazz.getName(), Integer.toString(id));

        }

        return object;
    }

    private Map getMap(Class type) {
        synchronized (store) {
            Map map = (Map) store.get(type);
            if (map == null) {
                map = new HashMap();
                store.put(type, map);
            }
            return map;
        }
    }


}