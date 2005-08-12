package org.apache.maven.continuum.execution;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.project.ContinuumProject;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumBuildExecutor
{
    String ROLE = ContinuumBuildExecutor.class.getName();

    //TODO: i know we won't have many more build executors but this really isn't very
    // scalable having constants for specific builders in the api
    String ANT_EXECUTOR_ID = "ant";

    String MAVEN_ONE_EXECUTOR_ID = "maven-one";

    String MAVEN_TWO_EXECUTOR_ID = "maven-two";

    String SHELL_EXECUTOR_ID = "shell";

    /**
     * @deprecated you need to give a build definition
     */
    ContinuumBuildExecutionResult build( ContinuumProject project, File buildOutput )
        throws ContinuumBuildExecutorException;

    ContinuumBuildExecutionResult build( ContinuumProject project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException;

    void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumBuildExecutorException;
}
