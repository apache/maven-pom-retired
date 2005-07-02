package org.apache.maven.continuum;

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

import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.scm.CheckOutScmResult;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
 public interface Continuum
{
    String ROLE = Continuum.class.getName();

    void removeProject( String projectId )
        throws ContinuumException;

    ContinuumProject getProject( String projectId )
        throws ContinuumException;

    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    Collection getProjects()
        throws ContinuumException;

    Collection getProjectsWithFailures()
        throws ContinuumException;

    Collection getProjectsWithErrors()
        throws ContinuumException;

    ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    void buildProjects()
        throws ContinuumException;

    void buildProjects( boolean force )
        throws ContinuumException;

    void buildProject( String projectId )
        throws ContinuumException;

    void buildProject( String projectId, boolean force )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    ContinuumBuild getBuild( String buildId )
        throws ContinuumException;

    Collection getBuildsForProject( String projectId )
        throws ContinuumException;

    Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Maven 2.x projects.
    // ----------------------------------------------------------------------

    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException;

    String addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException;

    MavenTwoProject getMavenTwoProject( String id )
        throws ContinuumException;

    void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException;

    String addMavenOneProject( MavenOneProject project )
        throws ContinuumException;

    MavenOneProject getMavenOneProject( String id )
        throws ContinuumException;

    void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    String addAntProject( AntProject project )
        throws ContinuumException;

    AntProject getAntProject( String id )
        throws ContinuumException;

    void updateAntProject( AntProject project )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    String addShellProject( ShellProject project )
        throws ContinuumException;

    ShellProject getShellProject( String id )
        throws ContinuumException;

    void updateShellProject( ShellProject project )
        throws ContinuumException;
}
