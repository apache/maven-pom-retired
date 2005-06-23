package org.apache.maven.continuum.it;

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

import java.io.File;

import org.codehaus.plexus.application.PlexusApplicationHost;
import org.codehaus.plexus.application.ApplicationServer;
import org.codehaus.plexus.application.profile.ApplicationRuntimeProfile;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.classworlds.ClassWorld;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumIt
{
    public static void main( String[] args )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Set up the enviroment
        // ----------------------------------------------------------------------

        File basedir = new File( new File( "" ).getAbsolutePath() );

        File plexusHome = new File( basedir, "../continuum-plexus-application/target/plexus-test-runtime" );

        System.setProperty( "plexus.home", plexusHome.getAbsolutePath() );

        // ----------------------------------------------------------------------
        // Start the application host
        // ----------------------------------------------------------------------

        PlexusApplicationHost applicationHost = new PlexusApplicationHost();

        ClassWorld classWorld = new ClassWorld();

//        String configurationResource = new File( basedir, "../continuum-plexus-application/src/conf/application.xml" ).getAbsolutePath();

        String configurationResource = new File( basedir, "src/main/resources/server.xml" ).getAbsolutePath();

        applicationHost.start( classWorld, configurationResource );

        ApplicationServer server = applicationHost.getApplicationServer();

        ApplicationRuntimeProfile profile = server.getApplicationRuntimeProfile( "continuum" );

        PlexusContainer container = profile.getContainer();

        applicationHost.shutdown();
    }

    private static void doTests()
    {
    }
}
