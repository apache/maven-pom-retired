package org.apache.maven.continuum.web.action;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugins.release.ReleaseResult;
import org.apache.maven.plugins.release.versions.DefaultVersionInfo;
import org.apache.maven.plugins.release.versions.VersionInfo;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="prepareRelease"
 */
public class PrepareReleaseAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String name;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private List projects = new ArrayList();

    private List projectKeys;

    private List devVersions;

    private List relVersions;

    private String prepareGoals;

    private ReleaseResult result;

    private ContinuumReleaseManagerListener listener;

    public String execute()
        throws Exception
    {
        Project project = getContinuum().getProject( projectId );
        scmUsername = project.getScmUsername();
        scmPassword = project.getScmPassword();
        scmTag = project.getScmTag();
        String scmUrl = project.getScmUrl();

        //skip scm:provider in scm url
        int idx = scmUrl.indexOf( ":", 4 ) + 1;
        scmUrl = scmUrl.substring( idx );

        if ( scmUrl.endsWith( "/trunk" ) )
        {
            scmTagBase = scmUrl.substring( 0 , scmUrl.lastIndexOf( "/trunk" ) ) + "/branches";
        }
        else
        {
            scmTagBase = scmUrl.substring( idx );
        }
        prepareGoals = "clean integration-test";

        getReleasePluginParameters( project.getWorkingDirectory(), "pom.xml" );

        processProject( project.getWorkingDirectory(), "pom.xml" );

        return "prompt";
    }

    private void getReleasePluginParameters( String workingDirectory, String pomFilename )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getBuild() != null && model.getBuild().getPlugins() != null )
        {
            for( Iterator plugins = model.getBuild().getPlugins().iterator(); plugins.hasNext(); )
            {
                Plugin plugin = (Plugin) plugins.next();

                if ( plugin.getGroupId() != null && plugin.getGroupId().equals( "org.apache.maven.plugins" ) &&
                     plugin.getArtifactId() != null && plugin.getArtifactId().equals( "maven-release-plugin" ) )
                {
                    Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

                    Xpp3Dom configuration = dom.getChild( "releaseLabel" );
                    if ( configuration != null )
                    {
                        scmTag = configuration.getValue();
                    }

                    configuration = dom.getChild( "tag" );
                    if ( configuration != null )
                    {
                        scmTag = configuration.getValue();
                    }

                    configuration = dom.getChild( "tagBase" );
                    if ( configuration != null )
                    {
                        scmTagBase = configuration.getValue();
                    }

                    configuration = dom.getChild( "preparationGoals" );
                    if ( configuration != null )
                    {
                        prepareGoals = configuration.getValue();
                    }
                }
            }
        }
    }

    public String doPrepare()
        throws Exception
    {
        listener = new DefaultReleaseManagerListener();

        Project project = getContinuum().getProject( projectId );

        name = project.getName();
        if ( name == null )
        {
            name = project.getArtifactId();
        }

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        releaseId = releaseManager.prepare( project, getReleaseProperties(), getRelVersionMap(),
                                            getDevVersionMap(), listener );

        return "initialized";
    }

    public String viewResult()
        throws Exception
    {
        result = (ReleaseResult) getContinuum().getReleaseManager().getReleaseResults().get( releaseId );

        return "viewResult";
    }

    public String checkProgress()
        throws Exception
    {
        String status;

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );

        if ( listener != null )
        {
            if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
            {
                releaseManager.getListeners().remove( releaseId );

                result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );

                status = "finished";
            }
            else
            {
                status = "inProgress";
            }
        }
        else
        {
            throw new Exception( "There is no release on-going or finished with id: " + releaseId );
        }

        return status;
    }

    private void processProject( String workingDirectory, String pomFilename )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getGroupId() == null )
        {
            model.setGroupId( model.getParent().getGroupId() );
        }

        if ( model.getVersion() == null )
        {
            model.setVersion( model.getParent().getVersion() );
        }

        setProperties( model );

        for( Iterator modules = model.getModules().iterator(); modules.hasNext(); )
        {
            processProject( workingDirectory + "/" + modules.next().toString(), "pom.xml" );
        }
    }

    private void setProperties( Model model )
        throws Exception
    {
        Map params = new HashMap();

        params.put( "key", model.getGroupId() + ":" + model.getArtifactId() );

        if ( model.getName() == null )
        {
            model.setName( model.getArtifactId() );
        }
        params.put( "name", model.getName() );

        VersionInfo version = new DefaultVersionInfo( model.getVersion() );

        params.put( "release", version.getReleaseVersionString() );
        params.put( "dev", version.getNextVersion().getSnapshotVersionString() );

        projects.add( params );
    }

    private Map getDevVersionMap()
    {
        return getVersionMap( projectKeys, devVersions );
    }

    private Map getRelVersionMap()
    {
        return getVersionMap( projectKeys, relVersions );
    }

    private Map getVersionMap( List keys, List versions )
    {
        Map versionMap = new HashMap();

        for ( int idx = 0; idx < keys.size(); idx++ )
        {
            String key = keys.get( idx ).toString();
            String version = versions.get( idx ).toString();

            versionMap.put( key, version );
        }

        return versionMap;
    }

    private Properties getReleaseProperties()
    {
        Properties p = new Properties();

        if ( StringUtils.isNotEmpty( scmUsername ) )
        {
            p.setProperty( "username", scmUsername );
        }

        if ( StringUtils.isNotEmpty( scmPassword ) )
        {
            p.setProperty( "password", scmPassword );
        }

        p.setProperty( "tag", scmTag );
        p.setProperty( "tagBase", scmTagBase );
        p.setProperty( "prepareGoals", prepareGoals );

        return p;
    }

    public List getProjectKeys()
    {
        return projectKeys;
    }

    public void setProjectKeys( List projectKeys )
    {
        this.projectKeys = projectKeys;
    }

    public List getDevVersions()
    {
        return devVersions;
    }

    public void setDevVersions( List devVersions )
    {
        this.devVersions = devVersions;
    }

    public List getRelVersions()
    {
        return relVersions;
    }

    public void setRelVersions( List relVersions )
    {
        this.relVersions = relVersions;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public String getScmTagBase()
    {
        return scmTagBase;
    }

    public void setScmTagBase( String scmTagBase )
    {
        this.scmTagBase = scmTagBase;
    }

    public List getProjects()
    {
        return projects;
    }

    public void setProjects( List projects )
    {
        this.projects = projects;
    }

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( DefaultReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public String getPrepareGoals()
    {
        return prepareGoals;
    }

    public void setPrepareGoals( String prepareGoals )
    {
        this.prepareGoals = prepareGoals;
    }
}
