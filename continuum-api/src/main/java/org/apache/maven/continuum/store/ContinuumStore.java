package org.apache.maven.continuum.store;

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

import java.util.Collection;
import java.util.List;

import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.ContinuumException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumStore
{
    String ROLE = ContinuumStore.class.getName();

    // ----------------------------------------------------------------------
    // ContinuumProject Mutators
    // ----------------------------------------------------------------------

    String addProject( ContinuumProject project )
        throws ContinuumStoreException;

    void removeProject( String projectId )
        throws ContinuumStoreException;

    void updateProject( ContinuumProject project )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // ContinuumProject Queries
    // ----------------------------------------------------------------------

    Collection getAllProjects()
        throws ContinuumStoreException;

    ContinuumProject getProjectByName( String name )
        throws ContinuumStoreException;

    ContinuumProject getProjectByScmUrl( String scmUrl )
        throws ContinuumStoreException;

    ContinuumProject getProject( String projectId )
        throws ContinuumStoreException;

    ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    String addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException;

    void updateBuild( ContinuumBuild build )
        throws ContinuumStoreException;

    ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException;

    ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException;

    Collection getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException;

    List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Notifiers
    // ----------------------------------------------------------------------

    void removeNotifier( Object notifier )
        throws ContinuumStoreException;

    void storeNotifier( Object notifier )
        throws ContinuumStoreException;
}
