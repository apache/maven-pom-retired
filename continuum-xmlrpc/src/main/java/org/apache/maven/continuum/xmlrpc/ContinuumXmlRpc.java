package org.apache.maven.continuum.xmlrpc;

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

import java.util.Hashtable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumXmlRpc
{
    String ROLE = ContinuumXmlRpc.class.getName();

    // ----------------------------------------------------------------------
    // Project handling
    // ----------------------------------------------------------------------

    Hashtable getProject( String projectId );

    Hashtable updateProject( String projectId,
                             String name,
                             String scmUrl,
                             String nagEmailAddress,
                             String version,
                             String commandLineArguments );

    Hashtable updateProjectConfiguration( String projectId,
                                          Hashtable configuration );

    Hashtable getAllProjects();

    Hashtable removeProject( String projectId );

    // ----------------------------------------------------------------------
    // Build handling
    // ----------------------------------------------------------------------

    Hashtable buildProject( String projectId,
                            boolean force );

    Hashtable getBuildsForProject( String projectId,
                                   int start,
                                   int end );

    Hashtable getBuild( String buildId );

    Hashtable getBuildResult( String buildId );

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    Hashtable addMavenTwoProject( String url );

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    Hashtable addMavenOneProject( String url );


    // ----------------------------------------------------------------------
    // Ant projects
    // ----------------------------------------------------------------------

    Hashtable addAntProject( String scmUrl,
                             String projectName,
                             String nagEmailAddress,
                             String version,
                             String commandLineArguments,
                             Hashtable configuration );

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    Hashtable addShellProject( String scmUrl,
                               String projectName,
                               String nagEmailAddress,
                               String version,
                               String commandLineArguments,
                               Hashtable configuration );
}
