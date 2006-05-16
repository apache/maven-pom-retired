package org.apache.maven.continuum.it;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AntIntegrationTest
    extends AbstractIntegrationTest
{
    public void testAntSvn()
        throws Exception
    {
        Continuum continuum = getContinuum();

        initializeSvnRoot();

        progress( "Initializing Ant SVN project" );

        File root = getItFile( "ant-svn" );

        initAntProject( root );

        svnImport( root, "ant-svn", getSvnRoot() );

        progress( "Adding Ant SVN project" );

        Project p = new Project();
        p.setScmUrl( makeScmUrl( "svn", getSvnRoot(), "ant-svn" ) );
        p.setName( "Ant SVN Project" );
//        p.getNotifiers().add( makeMailNotifier( email ) );
        p.setVersion( "3.0" );

        BuildDefinition bd = new BuildDefinition();
        bd.setArguments( "-v" );
        bd.setBuildFile( "build.xml" );
        bd.setGoals( "clean build" );
        bd.setDefaultForProject( true );
        
        p.addBuildDefinition( bd );

        int projectId = continuum.addProject( p, AntBuildExecutor.ID );

        waitForSuccessfulCheckout( projectId );

        Project project = continuum.getProject( projectId );

        assertProject( projectId, "Ant SVN Project", "3.0", "-v", "ant", project );

        progress( "Building SVN Ant project" );

        int buildId = buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED ).getId();

        assertSuccessfulAntBuild( buildId, project.getId() );

        removeProject( projectId );
    }

    public void testAntCvs()
        throws Exception
    {
        Continuum continuum = getContinuum();

        initializeCvsRoot();

        progress( "Initializing Ant CVS project" );

        File root = getItFile( "ant-cvs" );

        initAntProject( root );

        cvsImport( root, "ant-cvs", getCvsRoot() );

        Project p = new Project();
        p.setScmUrl( makeScmUrl( "cvs", getCvsRoot(), "ant-cvs" ) );
        p.setName( "Ant CVS Project" );
//        p.getNotifiers().add( makeMailNotifier( email ) );
        p.setVersion( "3.0" );

        BuildDefinition bd = new BuildDefinition();
        bd.setArguments( "-debug" );
        bd.setBuildFile( "build.xml" );
        bd.setGoals( "clean build" );
        bd.setDefaultForProject( true );
        p.addBuildDefinition( bd );

        int projectId = continuum.addProject( p, AntBuildExecutor.ID );

        waitForSuccessfulCheckout( projectId );

        Project project = continuum.getProject( projectId );
        assertProject( projectId, "Ant CVS Project", "3.0", "-debug", AntBuildExecutor.ID, project );

        progress( "Building CVS Ant project" );

        int buildId = buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED ).getId();

        assertSuccessfulAntBuild( buildId, projectId );

        removeProject( projectId );
    }

    private ProjectNotifier makeMailNotifier( String address )
    {
        ProjectNotifier notifier = new ProjectNotifier();

        notifier.setType( "mail" );

        Properties configuration = new Properties();

        configuration.setProperty( "address", address );

        notifier.setConfiguration( configuration );

        return notifier;
    }

    private void initAntProject( File root )
        throws IOException
    {
        deleteAndCreateDirectory( root );

        File buildXml = new File( root, "build.xml" );

        FileUtils.fileWrite( buildXml.getAbsolutePath(), "<project default=\"foo\">\n" + "  <target name=\"build\">\n" +
            "    <property name=\"classes\" value=\"target/classes\"/>\n" + "    <mkdir dir=\"${classes}\"/>\n" +
            "    <javac srcdir=\"src/main/java\" destdir=\"${classes}\"/>\n" + "  </target>\n" +
            "  <target name=\"clean\">\n" + "    <delete dir=\"${classes}\"/>\n" + "  </target>\n" + "</project>" );

        assertTrue( new File( root, "src/main/java" ).mkdirs() );

        FileUtils.fileWrite( new File( root, "src/main/java/Foo.java" ).getAbsolutePath(), "class Foo { }" );
    }
}
