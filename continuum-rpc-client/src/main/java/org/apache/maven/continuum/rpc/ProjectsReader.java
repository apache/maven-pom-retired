package org.apache.maven.continuum.rpc;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author mkleint
 */
public class ProjectsReader
{

    private URL server;

    private Hashtable executorMap;

    /**
     * Creates a new instance of ProjectsReader
     */
    public ProjectsReader( URL serverUrl )
    {
        server = serverUrl;
        executorMap = new Hashtable();
        executorMap.put( "shell", "Shell" );
        executorMap.put( "ant", "Ant" );
        executorMap.put( "maven-1", "MavenOne" );
        executorMap.put( "maven2", "MavenTwo" );
    }

    public Project[] readProjects()
        throws XmlRpcException, IOException
    {
        XmlRpcClient client = new XmlRpcClient( server );
        Object obj = client.execute( "continuum.getProjects", new Vector() );
        Collection set = new ArrayList();
        if ( obj instanceof Hashtable )
        {
            Hashtable table = (Hashtable) obj;
            Vector projects = (Vector) table.get( "projects" );
            Iterator it = projects.iterator();
            while ( it.hasNext() )
            {
                Hashtable proj = (Hashtable) it.next();
                set.add( populateProject( proj, new Project() ) );
            }
        }
        else if ( obj instanceof XmlRpcException )
        {
            throw (XmlRpcException) obj;
        }

        return (Project[]) set.toArray( new Project[set.size()] );
    }

    public void refreshProject( Project proj )
        throws XmlRpcException, IOException
    {
        XmlRpcClient client = new XmlRpcClient( server );
        Vector vect = new Vector();
        vect.add( new Integer( proj.getId() ) );
        Object obj = client.execute( "continuum.getProject", vect );
        if ( obj instanceof Hashtable )
        {
            Hashtable table = (Hashtable) obj;
            populateProject( (Hashtable) table.get( "project" ), proj );
        }
        else if ( obj instanceof XmlRpcException )
        {
            throw (XmlRpcException) obj;
        }
    }

    public void buildProject( Project proj )
        throws XmlRpcException, IOException
    {
        XmlRpcClient client = new XmlRpcClient( server );
        Vector vect = new Vector();
        vect.add( new Integer( proj.getId() ) );
        //trigger
        vect.add( new Integer( 1 ) );
        Object obj = client.execute( "continuum.buildProject", vect );

        if ( obj instanceof XmlRpcException )
        {
            throw (XmlRpcException) obj;
        }
    }

    public void editProject( Project proj )
        throws XmlRpcException, IOException
    {
        XmlRpcClient client = new XmlRpcClient( server );
        Vector vect = new Vector();
        vect.add( projectToHashtable( proj ) );
        Object obj = client.execute( "continuum.update" + executorMap.get( proj.getExecutorId() ) + "Project", vect );
        if ( obj instanceof Hashtable )
        {
            Hashtable table = (Hashtable) obj;
            if ( !table.get( "result" ).equals( "ok" ) )
            {
                throw new RuntimeException( "Edit failed" );
            }
        }
        else if ( obj instanceof XmlRpcException )
        {
            throw (XmlRpcException) obj;
        }
    }

    public void addProject( Project proj )
        throws XmlRpcException, IOException
    {
        XmlRpcClient client = new XmlRpcClient( server );
        Vector vect = new Vector();
        vect.add( projectToHashtable( proj ) );
        Object obj = client.execute( "continuum.add" + executorMap.get( proj.getExecutorId() ) + "Project", vect );
        if ( obj instanceof Hashtable )
        {
            Hashtable table = (Hashtable) obj;
            if ( !table.get( "result" ).equals( "ok" ) )
            {
                throw new RuntimeException( "Edit failed" );
            }
        }
        else if ( obj instanceof XmlRpcException )
        {
            throw (XmlRpcException) obj;
        }
    }

    private Project populateProject( Hashtable hashtable, Project instance )
    {
        instance.setArtifactId( (String) hashtable.get( "artifactId" ) );
        instance.setGroupId( (String) hashtable.get( "groupId" ) );
        instance.setName( (String) hashtable.get( "name" ) );
        instance.setDescription( (String) hashtable.get( "description" ) );
        instance.setVersion( (String) hashtable.get( "version" ) );
        instance.setUrl( (String) hashtable.get( "url" ) );
        instance.setExecutorId( (String) hashtable.get( "executorId" ) );
        instance.setWorkingDirectory( (String) hashtable.get( "workingDirectory" ) );
        instance.setScmUsername( (String) hashtable.get( "scmUsername" ) );
        instance.setScmPassword( (String) hashtable.get( "scmPassword" ) );
        instance.setScmTag( (String) hashtable.get( "scmTag" ) );
        instance.setScmUrl( (String) hashtable.get( "scmUrl" ) );
        instance.setId( Integer.parseInt( (String) hashtable.get( "id" ) ) );
        instance.setLatestBuildId( Integer.parseInt( (String) hashtable.get( "latestBuildId" ) ) );
        instance.setState( Integer.parseInt( (String) hashtable.get( "state" ) ) );
        instance.setOldState( Integer.parseInt( (String) hashtable.get( "oldState" ) ) );
        instance.setBuildNumber( Integer.parseInt( (String) hashtable.get( "buildNumber" ) ) );
        Vector buildDefinitions = (Vector) hashtable.get( "buildDefinitions" );
        if ( buildDefinitions != null )
        {
            Iterator it = buildDefinitions.iterator();
            List defs = new ArrayList();
            while ( it.hasNext() )
            {
                Hashtable table = (Hashtable) it.next();
                BuildDefinition def = new BuildDefinition();
                def.setId( Integer.parseInt( (String) table.get( "id" ) ) );
                def.setArguments( (String) table.get( "arguments" ) );
                def.setBuildFile( (String) table.get( "buildFile" ) );
                def.setDefaultForProject( Boolean.getBoolean( (String) table.get( "defaultForProject" ) ) );
                def.setGoals( (String) table.get( "goals" ) );
                Vector prof = (Vector) table.get( "profile" );
                if ( prof != null )
                {
                    Profile profile = new Profile();
                    //TODO
                    def.setProfile( profile );
                }
                Object obj = table.get( "schedule" );
                Hashtable sched = (Hashtable) obj;
                if ( sched != null )
                {
                    Schedule schedule = new Schedule();
                    schedule.setActive( Boolean.getBoolean( (String) sched.get( "active" ) ) );
                    schedule.setCronExpression( (String) sched.get( "cronExpression" ) );
                    schedule.setDelay( Integer.parseInt( (String) sched.get( "delay" ) ) );
                    schedule.setDescription( (String) sched.get( "description" ) );
                    schedule.setId( Integer.parseInt( (String) sched.get( "id" ) ) );
                    schedule.setName( (String) sched.get( "name" ) );
                    def.setSchedule( schedule );
                }
                defs.add( def );
            }
            instance.setBuildDefinitions( defs );
        }
        Vector buildRes = (Vector) hashtable.get( "buildResults" );
        if ( buildRes != null )
        {
            Iterator it = buildRes.iterator();
            List results = new ArrayList();
            while ( it.hasNext() )
            {
                Hashtable res = (Hashtable) it.next();
                BuildResult result = new BuildResult();
                result.setBuildNumber( Integer.parseInt( (String) res.get( "buildNumber" ) ) );
                result.setEndTime( Long.parseLong( (String) res.get( "endTime" ) ) );
                result.setError( (String) res.get( "error" ) );
                result.setExitCode( Integer.parseInt( (String) res.get( "exitCode" ) ) );
                result.setId( Integer.parseInt( (String) res.get( "id" ) ) );
//TODO                result.setScmResult();
                result.setStartTime( Long.parseLong( (String) res.get( "startTime" ) ) );
                result.setState( Integer.parseInt( (String) res.get( "state" ) ) );
                result.setSuccess( Boolean.getBoolean( (String) res.get( "success" ) ) );
                result.setTrigger( Integer.parseInt( (String) res.get( "trigger" ) ) );
                results.add( result );
            }
            instance.setBuildResults( results );
        }
        Vector deps = (Vector) hashtable.get( "dependencies" );
        if ( deps != null )
        {
            Iterator it = deps.iterator();
            List vals = new ArrayList();
            while ( it.hasNext() )
            {
                Hashtable dep = (Hashtable) it.next();
                ProjectDependency dependency = new ProjectDependency();
                dependency.setArtifactId( (String) dep.get( "artifactId" ) );
                dependency.setGroupId( (String) dep.get( "groupId" ) );
                dependency.setVersion( (String) dep.get( "version" ) );
                vals.add( dependency );
            }
            instance.setDependencies( vals );
        }
        Hashtable par = (Hashtable) hashtable.get( "parent" );
        if ( par != null )
        {
            ProjectDependency parent = new ProjectDependency();
            parent.setArtifactId( (String) par.get( "artifactId" ) );
            parent.setGroupId( (String) par.get( "groupId" ) );
            parent.setVersion( (String) par.get( "version" ) );
            instance.setParent( parent );
        }
        Vector devs = (Vector) hashtable.get( "developers" );
        if ( devs != null )
        {
            Iterator it = devs.iterator();
            List vals = new ArrayList();
            while ( it.hasNext() )
            {
                Hashtable dep = (Hashtable) it.next();
                ProjectDeveloper developer = new ProjectDeveloper();
                developer.setContinuumId( Integer.parseInt( (String) dep.get( "continuumId" ) ) );
                developer.setName( (String) dep.get( "name" ) );
                developer.setEmail( (String) dep.get( "email" ) );
                developer.setScmId( (String) dep.get( "scmId" ) );
                vals.add( developer );
            }
            instance.setDevelopers( vals );
        }
//        Vector nots = (Vector)hashtable.get("notifiers");
//        if (nots != null) {
//            Iterator it = nots.iterator();
//            List vals = new ArrayList();
//            while (it.hasNext()) {
//                Hashtable not = (Hashtable)it.next();
//                ProjectNotifier notifier = new ProjectNotifier();
//                //TODO...
////                notifier.setConfiguration();
//                notifier.setType((String)not.get("type"));
//                vals.add(notifier);
//            }
//            instance.setNotifiers(vals);
//        }
        Hashtable checkout = (Hashtable) hashtable.get( "checkoutResult" );
        if ( checkout != null )
        {
            ScmResult res = new ScmResult();
            res.setSuccess( Boolean.getBoolean( (String) checkout.get( "success" ) ) );
            res.setCommandLine( (String) checkout.get( "commandLine" ) );
//TODO            res.setChanges();
            res.setCommandOutput( (String) checkout.get( "commandOutput" ) );
            res.setException( (String) checkout.get( "exception" ) );
            res.setProviderMessage( (String) checkout.get( "providerMessage" ) );
            instance.setCheckoutResult( res );

        }
        return instance;
    }

    private Hashtable projectToHashtable( Project project )
    {
        Hashtable map = new Hashtable();
        mapPut( map, "id", "" + project.getId() );
        mapPut( map, "name", project.getName() );
        mapPut( map, "version", project.getVersion() );
        mapPut( map, "executorId", project.getExecutorId() );
        mapPut( map, "workingDirectory", project.getWorkingDirectory() );
        mapPut( map, "scmUsername", project.getScmUsername() );
        mapPut( map, "scmPassword", project.getScmPassword() );
        mapPut( map, "scmTag", project.getScmTag() );
        mapPut( map, "scmUrl", project.getScmUrl() );
        mapPut( map, "name", project.getName() );
        return map;
    }

    private static void mapPut( Hashtable table, String key, Object value )
    {
        if ( value != null )
        {
            table.put( key, value );
        }
    }

}
