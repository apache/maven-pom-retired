package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;

import java.util.Iterator;
import java.util.List;
/*
 * Copyright 2005 The Apache Software Foundation.
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

/**
 * AbstractBuildDefinitionContinuumAction:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 */
public abstract class AbstractBuildDefinitionContinuumAction
    extends AbstractContinuumAction
{

    /**
     * @plexus.requirement
     */
    ContinuumStore store;

    protected void resolveDefaultBuildDefinitionsForProject( BuildDefinition buildDefinition, Project project )
        throws ContinuumException
    {
        try
        {
            // if buildDefinition passed in is not default then we are done
            if ( buildDefinition.isDefaultForProject() )
            {
                BuildDefinition storedDefinition =
                    store.getDefaultBuildDefinitionForProject( project.getId() );

                if ( storedDefinition != null )
                {
                    storedDefinition.setDefaultForProject( false );

                    store.storeBuildDefinition( storedDefinition );
                }
            }
        }
        catch ( ContinuumObjectNotFoundException nfe )
        {
            getLogger().debug( getClass().getName() + ": safely ignoring the resetting of old build definition becuase it didn't exist" );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "error updating old default build definition", cse );
        }
    }

    /**
     * resolves build definition defaults between project groups and projects
     *
     * 1) project groups have default build definitions
     * 2) if project has default build definition, that overrides project group definition
     * 3) changing parent default build definition does not effect project if it has a default declared
     * 4) project groups much have a default build definition
     *
     * @param buildDefinition
     * @param projectGroup
     */
    protected void resolveDefaultBuildDefinitionsForProjectGroup( BuildDefinition buildDefinition,
                                                                  ProjectGroup projectGroup )
        throws ContinuumException
    {
        try
        {
            BuildDefinition storedDefinition =
                                store.getDefaultBuildDefinitionForProjectGroup( projectGroup.getId() );

            // if buildDefinition passed in is not default then we are done
            if ( buildDefinition.isDefaultForProject() )
            {
                if ( storedDefinition != null && storedDefinition.getId() != buildDefinition.getId() )  
                {
                    storedDefinition.setDefaultForProject( false );

                    store.storeBuildDefinition( storedDefinition );
                }
            }
            else
            {
                //make sure we are not wacking out default build definition, that would be bad
                if ( buildDefinition.getId() == storedDefinition.getId() )
                {
                    getLogger().info( "processing this build definition would result in no default build definition for project group" );
                    throw new ContinuumException( "processing this build definition would result in no default build definition for project group" );
                }
            }
        }
        catch ( ContinuumStoreException cse )
        {
            getLogger().info( "error updating old default build definition", cse );
            throw new ContinuumException( "error updating old default build definition", cse );
        }
    }

    /**
     * attempts to walk through the list of build definitions and upon finding a match update it with the
     * information in the BuildDefinition object passed in.
     *
     * @param buildDefinitions
     * @param buildDefinition
     * @throws ContinuumException
     */
    protected void updateBuildDefinitionInList( List buildDefinitions, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        try
        {
            BuildDefinition storedDefinition = null;

            for ( Iterator i = buildDefinitions.iterator(); i.hasNext(); )
            {
                BuildDefinition bd = (BuildDefinition) i.next();

                if ( bd.getId() == buildDefinition.getId() )
                {
                    storedDefinition = bd;
                }
            }

            if ( storedDefinition != null )
            {
                storedDefinition.setGoals( buildDefinition.getGoals() );
                storedDefinition.setArguments( buildDefinition.getArguments() );
                storedDefinition.setBuildFile( buildDefinition.getBuildFile() );

                // special case of this is resolved in the resolveDefaultBuildDefinitionsForProjectGroup method
                storedDefinition.setDefaultForProject( buildDefinition.isDefaultForProject() );

                Schedule schedule;
                if ( buildDefinition.getSchedule() == null )
                {
                    try
                    {
                        schedule = store.getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME );
                    }
                    catch ( ContinuumStoreException e )
                    {
                        throw new ContinuumException( "Can't get default schedule.", e );
                    }
                }
                else
                {
                    schedule = store.getSchedule( buildDefinition.getSchedule().getId() );
                }

                storedDefinition.setSchedule( schedule );

                store.storeBuildDefinition( storedDefinition );
            }
            else
            {
                throw new ContinuumException( "failed update, build definition didn't exist in project group" );
            }
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "error in accessing or storing build definition" );
        }
    }
}
