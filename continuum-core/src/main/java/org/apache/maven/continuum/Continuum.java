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

import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * This is the main entry point for Continuum. Projects are added to Continuum
 * by providing an URL to the metadata for project. The metadata for a project
 * must contain the following information:
 * <p/>
 * o project name
 * o project id
 * o SCM information
 * o email notification list
 * o project developers
 */
public interface Continuum
{
    String ROLE = Continuum.class.getName();

    //TODO: an URL converter in OGNL would be nice.

    String addProjectFromUrl( String url, String builder )
        throws ContinuumException;

    String addProjectFromUrl( URL url, String builder )
        throws ContinuumException;

    String addProjectFromScm( String scmUrl,
                              String builderType,
                              String projectName,
                              String nagEmailAddress,
                              String version,
                              Properties configuration )
        throws ContinuumException;

    void removeProject( String projectId )
        throws ContinuumException;

    void updateProjectFromScm( String projectId )
        throws ContinuumException;

    void updateProjectConfiguration( String projectId, Properties configuration )
        throws ContinuumException;

    ContinuumProject getProject( String projectId )
        throws ContinuumException;

    Iterator getAllProjects( int start, int end )
        throws ContinuumException;

    String buildProject( String projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    Iterator getProjects()
        throws ContinuumStoreException;

    ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Different project types
    // ----------------------------------------------------------------------

    void addAntProject( AntProject project )
        throws ContinuumException;

    AntProject getAntProject( String id )
        throws ContinuumException;

    void updateAntProject( AntProject project )
        throws ContinuumException;

    // Maven 1

    void addMavenOneProject( MavenOneProject project )
        throws ContinuumException;

    MavenOneProject getMavenOneProject( String id )
        throws ContinuumException;

    void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException;

    // Maven 2

    void addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException;

    MavenTwoProject getMavenTwoProject( String id )
        throws ContinuumException;

    void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException;

    // Shell

    void addShellProject( ShellProject project )
        throws ContinuumException;

    ShellProject getShellProject( String id )
        throws ContinuumException;

    void updateShellProject( ShellProject project )
        throws ContinuumException;
}
