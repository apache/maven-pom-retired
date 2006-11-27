package org.apache.maven.continuum.project.builder.cc;

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

import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CruiseControlProjectBuilderTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                                                   CruiseControlProjectBuilder.ID );

        URL url = getTestFile( "src/test/resources/config.xml" ).toURL();

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( url, null, null );
    }
}
