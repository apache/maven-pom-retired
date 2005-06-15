package org.apache.maven.continuum.project;

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

import javax.jdo.PersistenceManagerFactory;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumJPoxStoreTest
    extends PlexusTestCase
{
    public void testCascadingDelete()
        throws Exception
    {
        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        ContinuumJPoxStore store = new ContinuumJPoxStore( pmf );

        ContinuumProject p;

        ContinuumBuild build;

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        p = new MavenTwoProject();

        p.setName( "Yo Yo Project" );

        Object oid = store.storeContinuumProject( p );

        p = store.getContinuumProjectByJdoId( oid, true );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = new ContinuumBuild();

        build.setSuccess( true );

        build.setExitCode( 1 );

        build.setProject( p );

        build = new ContinuumBuild();

        build.setSuccess( true );

        build.setExitCode( 2 );

        build.setProject( p );

        store.storeContinuumProject( p );

        // ----------------------------------------------------------------------
        // Assert that the objects are there
        // ----------------------------------------------------------------------

        Collection projects = store.getContinuumProjectCollection( true, "", "" );

        assertEquals( 1, projects.size() );

        p = (ContinuumProject) projects.iterator().next();

        Collection builds = p.getBuilds();

        assertEquals( 2, builds.size() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store.deleteContinuumProject( p.getId() );

        assertEquals( 0, store.getContinuumProjectCollection( true, "", "" ).size() );

        assertEquals( 0, store.getContinuumBuildCollection( true, "", "" ).size() );
    }
}
