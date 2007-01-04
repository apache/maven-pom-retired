package org.apache.maven.continuum.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.wagon.WagonContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.notification.notifier.Notifier;

/**
 * @author <a href="mailto:nramirez@exist">Napoleon Esmundo C. Ramirez</a>
 */
public class WagonContinuumNotifierTest
    extends PlexusTestCase
{
    private ServletServer server;
    
    private Notifier notifier;
    
    private Project project;
    
    private BuildResult build;
    
    private BuildDefinition buildDefinition;
    
    private Map context;
    
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        server = (ServletServer) lookup( ServletServer.ROLE );
        notifier = (Notifier) lookup( Notifier.ROLE, "wagon" );
        
        project = new Project();
        project.setId( 2 );
        
        build = new BuildResult();
        build.setId( 1 );
        build.setProject( project );
        build.setStartTime( System.currentTimeMillis() );
        build.setEndTime( System.currentTimeMillis() + 1234567 );
        build.setState( ContinuumProjectState.OK );
        build.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
        build.setExitCode( 0 );
        
        buildDefinition = new BuildDefinition();
        buildDefinition.setBuildFile( "pom.xml" );
        
        context = new HashMap();
        context.put( ContinuumNotificationDispatcher.CONTEXT_PROJECT, project );
        context.put( ContinuumNotificationDispatcher.CONTEXT_BUILD, build );
        context.put( WagonContinuumNotifier.KEY_BUILD_DEFINITION, buildDefinition );
        
        String basedir = System.getProperty( "basedir" );
        if ( basedir == null )
        {
            throw new Exception( "basedir must be defined" );
        }
    }
    
    public void testSendNotification()
        throws Exception
    {
        notifier.sendNotification( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE, new HashSet(), new HashMap(), context );
    }
    
    protected void tearDown()
        throws Exception
    {
        release( server );
    }
    
    
}
