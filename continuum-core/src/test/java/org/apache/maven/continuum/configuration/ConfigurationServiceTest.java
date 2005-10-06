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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ConfigurationServiceTest
    extends AbstractContinuumTest
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

        assertEquals( "build-output", service.getBuildOutputDirectory().getName() );

        assertEquals( "working-directory", service.getWorkingDirectory().getName() );

        service.store();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        service.load();
    }
}
