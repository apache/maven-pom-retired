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

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class BuildOutputExtension extends AbstractLogEnabled implements BuildAgentExtension {

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public void preProcess(Map build) {
    }

    public void postProcess(Map build, Map results) {
        File file = null;
        try {
            int projectId = AbstractContinuumAction.getProjectId(build);

            Project project = store.getProject(projectId);

            BuildResult buildResult = store.getBuildResult(project.getLatestBuildId());

            file = configurationService.getBuildOutputFile(buildResult.getId(), projectId);

        } catch (ContinuumStoreException e) {

            getLogger().error("Unable to retrieve info from the ContinuumStore.", e);

            return;

        } catch (ConfigurationException e) {

            getLogger().error("Unable to get the build output file from the configuration service.", e);

            return;
        }

        if (!file.exists()) {

            getLogger().warn("File to include doesn't exist: " + file.getAbsolutePath());

            return;
        }

        try {

            getLogger().debug("Reading " + file.getAbsolutePath());

            byte[] bytes = GZipUtils.fileRead(file);

            getLogger().info("Including " + bytes.length + " - " + file.getAbsolutePath());

            results.put("build.output-file.gz", bytes);

        }
        catch (IOException e) {
            getLogger().warn("Error reading file to include: " + file.getAbsolutePath(), e);
        }


    }

}
