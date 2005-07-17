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

import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ModelloJPoxContinuumStoreTest;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.AbstractContinuumTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumNotificationDispatcherTest
    extends AbstractContinuumTest
{
    public void testNotificationDispatcher()
        throws Exception
    {
        ContinuumNotificationDispatcher notificationDispatcher =
            (ContinuumNotificationDispatcher) lookup( ContinuumNotificationDispatcher.ROLE );

        ContinuumStore store = getStore();

        String projectId = ModelloJPoxContinuumStoreTest.addMavenTwoProject( store,
                                                                             "Notification Dispatcher Test Project",
                                                                             "scm:notification/dispatcher" );

        ContinuumBuild build = new ContinuumBuild();
        build.setStartTime( System.currentTimeMillis() );
        build.setState( ContinuumProjectState.BUILDING );
        build.setForced( false );
        String buildId = store.addBuild( projectId, build );

        notificationDispatcher.buildComplete( store.getProject( projectId ), store.getBuild( buildId ) );
    }
}
