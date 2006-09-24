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

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;

/**
 * @version $Rev$ $Date$
 */
public class LogFailedBuildsExtension extends AbstractLogEnabled implements BuildResultsExtension, Startable {

    /**
     * @plexus.configuration
     */
    private String fileNameTemplate;
    private StringTemplate template;

    /**
     * @plexus.configuration
     */
    private String resultsDirectory;
    private File directory;

    /**
     * @plexus.configuration
     */
    private String dateFormat;
    private SimpleDateFormat dateFormatter;

    private StringTemplate header = new StringTemplate("#   Date: {date}\n#   Project: {project.name}-{project.version}\n#   OS: {os.name} - {os.version}\n#   Java: {java.version} - {java.vendor}\n#   Host: {host-name} {host-address}\n#   Contributor: {contributor} {admin-address}\n");

    public void start() throws StartingException {
        template = new StringTemplate(fileNameTemplate);
        directory = new File(resultsDirectory);
        directory.mkdirs();

        if (!directory.exists()) {
            throw new IllegalStateException("File specified does not exist. " + directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            throw new IllegalStateException("File specified is not a directory. " + directory.getAbsolutePath());
        }

        if (!directory.canWrite()) {
            throw new IllegalStateException("Directory specified is not writable. " + directory.getAbsolutePath());
        }

        getLogger().info("Include files will be written to " + directory.getAbsolutePath());
        dateFormatter = new SimpleDateFormat(dateFormat);
    }

    public void stop() throws StoppingException {
    }

    public void execute(Map context) throws Exception {

        ContinuumStore store = AbstractContinuumAgentAction.getContinuumStore(context);

        int projectId = AbstractContinuumAction.getProjectId(context);

        Project project = store.getProject(projectId);

        BuildResult buildResult = store.getBuildResult(project.getLatestBuildId());

        int exitCode = buildResult.getExitCode();

        getLogger().debug(context.get("build.name") + " " + context.get("build.id") + " - exit code (" + exitCode + ")");

        if (exitCode == 0) {
            return;
        }

        byte[] bytes = (byte[]) context.get("build.output-file.gz");

        if (bytes == null) {

            getLogger().debug("No build output to write.");

            return;
        }


        Map map = new HashMap();

        map.putAll(context);

        map.put("date", dateFormatter.format(new Date()));

        String fileName = template.apply(map);

        File file = new File(directory, fileName);

        File parent = file.getParentFile();

        parent.mkdirs();

        try {

            FileUtils.fileWrite(file.getAbsolutePath(), header.apply(map));

            GZipUtils.fileAppend(file, bytes);

        } catch (IOException e) {
            getLogger().error("Could not write to file " + file.getAbsolutePath(), e);
        }

        getLogger().info("Wrote build ouput (" + file.length() + " bytes) to " + file.getAbsolutePath());
    }
}
