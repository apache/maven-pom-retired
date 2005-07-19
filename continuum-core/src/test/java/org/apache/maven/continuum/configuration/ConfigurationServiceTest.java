package org.apache.maven.continuum.configuration;

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

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.apache.maven.continuum.profile.ContinuumJdk;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class ConfigurationServiceTest
    extends PlexusTestCase
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        File templateConfiguration = new File( getBasedir(), "src/test/resources/configuration.xml" );

        File testConfiguration = new File( getBasedir(), "target/configuration.xml" );

        FileUtils.copyFile( templateConfiguration, testConfiguration );
    }

    public void testConfigurationService()
        throws Exception
    {
        ConfigurationService service = (ConfigurationService) lookup( ConfigurationService.ROLE );

        service.load();

        assertEquals( "http://localhost:8080/continuum/servlet/continuum", service.getUrl() );

        assertEquals( "build-output", service.getBuildOutputDirectory().getName() );

        ContinuumJdk jdk = new ContinuumJdk();

        jdk.setVersion( "1.3" );

        jdk.setHome( "/jdks/1.3" );

        service.addJdk( jdk);

        jdk = new ContinuumJdk();

        jdk.setVersion( "1.4" );

        jdk.setHome( "/jdks/1.4" );

        service.addJdk( jdk);

        jdk = new ContinuumJdk();

        jdk.setVersion( "1.5" );

        jdk.setHome( "/jdks/1.5" );

        service.addJdk( jdk);

        service.store();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        service.load();

        Map jdks = service.getJdks();

        assertNotNull( jdks.get( "1.3" ) );

        jdk = (ContinuumJdk) jdks.get( "1.3" );

        assertEquals( "/jdks/1.3", jdk.getHome() );

        jdk = (ContinuumJdk) jdks.get( "1.4" );

        assertEquals( "/jdks/1.4", jdk.getHome() );

        jdk = (ContinuumJdk) jdks.get( "1.5" );

        assertEquals( "/jdks/1.5", jdk.getHome() );

    }
}