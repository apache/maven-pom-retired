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

import org.apache.maven.continuum.project.ContinuumJPoxStore;

import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloJPoxContinuumStoreTest
    extends AbstractContinuumStoreTest
{
    public ModelloJPoxContinuumStoreTest()
    {
        super( "modello", ModelloJPoxContinuumStore.class );
    }

    public void testTransactionHandling()
        throws Exception
    {
        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        ContinuumJPoxStore store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNull( store.getThreadState() );

        store.begin();

        assertNotNull( store.getThreadState() );

        store.commit();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.commit();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.commit();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.commit();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.rollback();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 2, store.getThreadState().getDepth() );

        store.rollback();

        assertNull( store.getThreadState() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.commit();

        assertNull( store.getThreadState() );
    }

    private void assertIsCommitted( ContinuumStore store )
    {
        ContinuumJPoxStore.ThreadState state = ( (ModelloJPoxContinuumStore) store ).getStore().getThreadState();

        assertNull( state );
    }
}
