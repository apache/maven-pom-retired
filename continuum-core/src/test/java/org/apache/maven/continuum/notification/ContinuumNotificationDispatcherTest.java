package org.apache.maven.continuum.notification;

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

import java.util.Properties;

import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ModelloJPoxContinuumStoreTest;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ContinuumNotificationDispatcherTest.java,v 1.2 2005/04/07 23:27:40 trygvis Exp $
 */
public class ContinuumNotificationDispatcherTest
    extends PlexusTestCase
{
    public void testNotificationDispatcher()
        throws Exception
    {
        ContinuumNotificationDispatcher notificationDispatcher =
            (ContinuumNotificationDispatcher) lookup( ContinuumNotificationDispatcher.ROLE );

        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = ModelloJPoxContinuumStoreTest.addProject( store, "Test Project" );

        String buildId = store.createBuild( projectId, false );

        ContinuumBuild build = store.getBuild( buildId );

        notificationDispatcher.buildComplete( build );
    }
}
