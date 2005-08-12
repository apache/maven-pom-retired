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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.xmlrpc.XmlRpcHelper;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractIntegrationTest
    extends PlexusTestCase
{
    private static final DateFormat progressDateFormat = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );

    private Date startTime;

    private File rootDirectory;

    private File cvsRoot;

    private File svnRoot;

    private File tempCoDir;

    private String remotingMethod;

    public static final String REMOTING_METHOD_JVM = "jvm";

    public static final String REMOTING_METHOD_XMLRPC = "xmlrpc";

    public static final String REMOTING_METHOD_XFIRE = "xfire";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected InputStream getConfiguration()
        throws Exception
    {
        Reader reader = new FileReader( getTestFile( "../continuum-plexus-application/src/conf/application.xml" ) );

        Properties properties = new Properties();

        properties.load( new FileInputStream( getTestFile( "../continuum-plexus-application/test.properties" ) ) );

        String s = IOUtil.toString( new InterpolationFilterReader( reader, properties, "@", "@" ) );

        return new ByteArrayInputStream( s.getBytes() );
    }

    protected void customizeContext( Context context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // This level of directory nesting is setup to mimic the continuum
        // application running in development mode where it picks up resources
        // in situ like forms, templates and localization resources.
        // ----------------------------------------------------------------------

        File plexusHome = getTestFile( "target/level1/level2/plexus-home" );

        if ( !plexusHome.isDirectory() )
        {
            assertTrue( plexusHome.mkdirs() );
        }

        // ----------------------------------------------------------------------
        // Use configuration file from the continuum-plexus-application
        // ----------------------------------------------------------------------

        File configurationSource = getTestFile( "../continuum-plexus-application/src/conf/configuration.xml" );

        File configurationForIT = new File( plexusHome, "conf/configuration.xml" );

        FileUtils.copyFile( configurationSource, configurationForIT );

        context.put( "plexus.home", plexusHome.getAbsolutePath() );
    }

    public final void setUp()
        throws Exception
    {
        startTime = new Date();

        super.setUp();

        // TODO: get this from System.getProperty()

        remotingMethod = REMOTING_METHOD_JVM;

        rootDirectory = getTestFile( "target/it" );

        cvsRoot = getItFile( "cvs-root" );

        svnRoot = getItFile( "svn-root" );

        tempCoDir = getItFile( "temp-co" );

        line();
        print( "Integration test settings" );
        line();
        print( "IT root: " + rootDirectory.getAbsolutePath() );
        print( "Remoting method: " + remotingMethod );
        print( "Store implementation: " + lookup( ContinuumStore.ROLE ).getClass() );
        line();

        deleteAndCreateDirectory( rootDirectory );

        progress( "Connecting to and starting Continuum" );
        Continuum continuum = getContinuum();

        progress( "Removing all existing projects from Continuum." );

        Collection collection = continuum.getAllProjects( 0, 0 );

        for ( Iterator it = collection.iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            continuum.removeProject( project.getId() );
        }

        AbstractContinuumTest.setUpConfigurationService( (ConfigurationService) lookup( ConfigurationService.ROLE ) );
    }

    public final void tearDown()
        throws Exception
    {
        Date endTime = new Date();

        super.tearDown();

        long diff = endTime.getTime() - startTime.getTime();

        progress( "Used " + diff + "ms" );
    }

    protected XmlRpcHelper getXmlRpcHelper()
        throws Exception
    {
        return (XmlRpcHelper) lookup( XmlRpcHelper.ROLE );
    }

    protected Continuum getContinuum()
        throws Exception
    {
        if ( remotingMethod.equals( REMOTING_METHOD_JVM ) )
        {
            return (Continuum) lookup( Continuum.ROLE );
        }
//        else if ( remotingMethod.equals( REMOTING_METHOD_XMLRPC ) )
//        {
//            return new ContinuumXmlRpcClient( getHost(), getPort(), getXmlRpcHelper() );
//        }

        fail( "Unsupported remoting method '" + remotingMethod + "'." );

        return null;
    }

    // ----------------------------------------------------------------------
    // Configuration. This should be read from a configuration file
    // ----------------------------------------------------------------------

    protected String getHost()
    {
        return "localhost";
    }

    protected int getPort()
    {
        return 8000;
    }

    protected String getEmail()
    {
        return System.getProperty( "user.name" ) + "@localhost";
    }

    // ----------------------------------------------------------------------
    // Paths
    // ----------------------------------------------------------------------

    protected File getCvsRoot()
    {
        return cvsRoot;
    }

    protected File getSvnRoot()
    {
        return svnRoot;
    }

    protected File getTempCoDir()
    {
        return tempCoDir;
    }

    protected File getItFile( String dir )
    {
        return new File( rootDirectory, dir );
    }

    // ----------------------------------------------------------------------
    // Component getters
    // ----------------------------------------------------------------------

    public ContinuumStore getStore()
        throws Exception
    {
        return (ContinuumStore) lookup( ContinuumStore.ROLE );
    }

    // ----------------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------------

    protected void system( File workingDirectory, String cmd, String arguments )
        throws CommandLineException
    {
        system( workingDirectory, cmd, new String[]{arguments} );
    }

    protected void system( File workingDirectory, String cmd, String[] arguments )
        throws CommandLineException
    {
        Commandline commandline = new Commandline();

        commandline.setExecutable( cmd );

        commandline.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        for ( int i = 0; i < arguments.length; i++ )
        {
            String argument = arguments[i];

            commandline.createArgument().setLine( argument );
        }

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode = CommandLineUtils.executeCommandLine( commandline, stdout, stderr );

        if ( exitCode != 0 )
        {
            System.err.println( "Error while executing command: " + commandline.toString() );
            System.err.println( "workingDirectory: " + workingDirectory.getAbsolutePath() );
            System.err.println( "Exit code: " + exitCode );

            System.err.println( "Standard output:" );
            line();
            System.err.println( stdout.getOutput() );
            line();
            System.err.println( "Standard Error:" );
            line();
            System.err.println( stderr.getOutput() );
            line();

            fail( "The command failed." );
        }
    }

    private void line()
    {
        System.err.println( "-------------------------------------------------------------------------------" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static void deleteAndCreateDirectory( File directory )
        throws IOException
    {
        if ( directory.isDirectory() )
        {
            FileUtils.deleteDirectory( directory );
        }

        assertTrue( "Could not make directory " + directory, directory.mkdirs() );
    }

    public static void cleanDirectory( String directory )
        throws IOException
    {
        cleanDirectory( new File( directory ) );
    }

    public static void cleanDirectory( File directory )
        throws IOException
    {
        if ( !directory.isDirectory() )
        {
            return;
        }

        FileUtils.cleanDirectory( directory );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected String getProjectId( ContinuumProjectBuildingResult buildingResult )
    {
        List warnings = buildingResult.getWarnings();

        if ( warnings.size() > 0 )
        {
            print( "Project building warnings: " );

            for ( Iterator it = warnings.iterator(); it.hasNext(); )
            {
                String warning = (String) it.next();

                print( warning );
            }

            fail( "There was warnings while building the project." );
        }

        List projects = buildingResult.getProjects();

        if ( projects.size() == 0 )
        {
            fail( "When adding a project a single project was expected to be added, no projects added." );
        }
        else if ( projects.size() > 1 )
        {
            String ids = "[" + ( (ContinuumProject) projects.get( 0 ) ).getId();

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                ids += ", " + project.getId();
            }

            ids += "]";

            fail( "When adding a project only a single project was expected to be added, project ids: " + ids );
        }

        return ( (ContinuumProject) projects.get( 0 ) ).getId();
    }

    public ContinuumBuild buildProject( String projectId, boolean force )
        throws Exception
    {
        int count = 600;

        int originalSize = getContinuum().getBuildsForProject( projectId ).size();

        getContinuum().buildProject( projectId, force );

        while ( true )
        {
            Collection builds = getContinuum().getBuildsForProject( projectId );

            if ( count == 0 )
            {
                fail( "Timeout while waiting for build. Project id: " + projectId );
            }

            if ( builds.size() != originalSize )
            {
                return (ContinuumBuild) builds.iterator().next();
            }

            count--;

            Thread.yield();
        }
    }

    public void removeProject( String projectId )
        throws Exception
    {
        getContinuum().removeProject( projectId );

        try
        {
            getContinuum().getProject( projectId );

            fail( "Expected ContinuumObjectNotFoundException after removing project '" + projectId + "'." );
        }
        catch ( ContinuumException t )
        {
            assertTrue( t.getCause() instanceof ContinuumObjectNotFoundException );
        }
    }

    public ContinuumProject waitForCheckout( String projectId )
        throws Exception
    {
        long timeout = 60 * 1000;

        long start = System.currentTimeMillis();

        ContinuumProject project = getContinuum().getProject( projectId );

        while ( project.getCheckoutResult() == null )
        {
            if ( System.currentTimeMillis() - start > timeout )
            {
                fail( "Timeout while waiting for project '" + project.getName() + "' to be checked out." );
            }

            Thread.yield();

            project = getContinuum().getProject( projectId );
        }

        return project;
    }

    public void waitForSuccessfulCheckout( String projectId )
        throws Exception
    {
        ContinuumProject project = waitForCheckout( projectId );

        String message = "The check out was not successful for project #" + project.getId() + ": ";

        if ( project.getCheckoutResult() == null )
        {
/* TODO
            print( "check out error message: " + project.getCheckOutErrorMessage() );
            print( "check out error exception: " );
            print( project.getCheckOutErrorException() );
*/

            fail( "project.scmResult == null" );
        }

/* TODO
        assertEquals( message + "Checkout error message != null", null, project.getCheckOutErrorMessage() );
        assertEquals( message + "Checkout error exception != null", null, project.getCheckOutErrorException() );
*/
        assertTrue( message + "scmResult.success != true", project.getCheckoutResult().isSuccess() );
    }

    public ContinuumBuild waitForBuild( String buildId )
        throws Exception
    {
        int timeout = 120 * 1000;
        long start = System.currentTimeMillis();

        ContinuumBuild build = getContinuum().getBuild( buildId );

        while ( build.getState() == ContinuumProjectState.UPDATING ||
            build.getState() == ContinuumProjectState.BUILDING )
        {
            if ( System.currentTimeMillis() - start > timeout )
            {
                fail( "Timeout while waiting for build #" + buildId + " to complete." );
            }

            Thread.yield();

            build = getContinuum().getBuild( buildId );
        }

        return build;
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertProject( String projectId, String name, String version, String commandLineArguments,
                               String executorId, ContinuumProject project )
    {
        assertEquals( "project.id", projectId, project.getId() );
        assertEquals( "project.name", name, project.getName() );
        assertEquals( "project.version", version, project.getVersion() );
        assertEquals( "project.executorId", executorId, project.getExecutorId() );
    }

    public void assertCheckedOutFiles( ContinuumProject project, String[] expectedCheckedOutFiles )
    {
        assertNotNull( "project.scmResult", project.getCheckoutResult() );

        ScmResult scmResult = project.getCheckoutResult();

        List actualCheckedOutFiles = scmResult.getFiles();

        if ( expectedCheckedOutFiles.length != actualCheckedOutFiles.size() )
        {
            print( "Expected files: " );

            for ( int i = 0; i < expectedCheckedOutFiles.length; i++ )
            {
                String checkedOutFile = expectedCheckedOutFiles[i];

                print( " " + checkedOutFile );
            }

            print( "Actual files: " );

            for ( Iterator it = actualCheckedOutFiles.iterator(); it.hasNext(); )
            {
                ScmFile scmFile = (ScmFile) it.next();

                print( " " + scmFile.getPath() );
            }

            assertEquals(
                "The expected and actual lists of checked out actualCheckedOutFiles doesn't have the same length.",
                expectedCheckedOutFiles.length, actualCheckedOutFiles.size() );
        }

        for ( int i = 0; i < expectedCheckedOutFiles.length; i++ )
        {
            String expectedCheckedOutFile = expectedCheckedOutFiles[i];

            ScmFile actualCheckedOutFile = (ScmFile) actualCheckedOutFiles.get( i );

            assertEquals( "File #" + i + " doesn't match the expected path.", expectedCheckedOutFile,
                          actualCheckedOutFile.getPath() );
        }
    }

    public ContinuumBuild assertSuccessfulNoBuildPerformed( String buildId )
        throws Exception
    {
        ContinuumBuild build = waitForBuild( buildId );

        assertEquals( "The build wasn't successful.", ContinuumProjectState.OK, build.getState() );

        return build;
    }

    public ContinuumBuild assertSuccessfulBuild( String buildId )
        throws Exception
    {
        ContinuumBuild build = waitForBuild( buildId );

        if ( build.getState() != ContinuumProjectState.OK )
        {
            print( "Build state: " + build.getState() );

            line();
            print( "Output" );
            line();
            print( getStore().getBuildOutput( buildId ) );
            line();

            fail( "The build was not successful" );
        }

        String output = getStore().getBuildOutput( buildId );

        assertNotNull( "Output was null.", output );

        return build;
    }

    public ContinuumBuild assertSuccessfulMaven1Build( String buildId )
        throws Exception
    {
        return assertSuccessfulAntBuild( buildId );
    }

    public ContinuumBuild assertSuccessfulMaven2Build( String buildId )
        throws Exception
    {
        return assertSuccessfulMaven1Build( buildId );
    }

    public ContinuumBuild assertSuccessfulAntBuild( String buildId )
        throws Exception
    {
        ContinuumBuild build = assertSuccessfulBuild( buildId );

        String output = getStore().getBuildOutput( buildId );

        if ( output.indexOf( "BUILD SUCCESSFUL" ) < 0 )
        {
            System.err.println( "output: " + output );
            fail( "Standard output didn't contain the 'BUILD SUCCESSFUL' message." );
        }

        return build;
    }

    public ContinuumBuild assertSuccessfulShellBuild( String buildId, String expectedStandardOutput )
        throws Exception
    {
        ContinuumBuild build = assertSuccessfulBuild( buildId );

        String output = getStore().getBuildOutput( buildId );

        assertEquals( "Standard output didn't contain the expected output.", expectedStandardOutput, output );

        return build;
    }

    // ----------------------------------------------------------------------
    // Scm Operations
    // ----------------------------------------------------------------------

    protected void initializeCvsRoot()
        throws IOException, CommandLineException
    {
        File cvsRoot = getCvsRoot();

        deleteAndCreateDirectory( cvsRoot );

        system( cvsRoot, "cvs", " -d " + cvsRoot.getAbsolutePath() + " init" );
    }

    protected void scmImport( File root, String artifactId, String scm, File scmRoot )
        throws CommandLineException
    {
        if ( "cvs".equals( scm ) )
        {
            cvsImport( root, artifactId, scmRoot );
        }
        else
        {
            fail( "Unknown scm '" + scm + "'." );
        }
    }

    protected void cvsImport( File root, String artifactId, File scmRoot )
        throws CommandLineException
    {
        system( root, "cvs",
                "-d " + scmRoot.getAbsolutePath() + " import -m yo_yo " + artifactId + " continuum_test start" );
    }

    protected void svnImport( File root, String artifactId, File svnRoot )
        throws CommandLineException
    {
        system( root, "svn", "import -m - . " + getFileUrl( getSvnRoot() ) + "/" + artifactId );
    }

    protected void cvsCheckout( File cvsRoot, String module, File coDir )
        throws CommandLineException
    {
        system( new File( getBasedir() ), "cvs",
                "-d " + cvsRoot.getAbsolutePath() + " checkout -d " + coDir.getAbsolutePath() + " " + module );
    }

    protected void cvsCommit( File coDir )
        throws CommandLineException
    {
        system( coDir, "cvs", new String[]{"commit -m ", "-"} );
    }

    protected void initializeSvnRoot()
        throws CommandLineException, IOException
    {
        File svnRoot = getSvnRoot();

        deleteAndCreateDirectory( svnRoot );

        system( svnRoot, "svnadmin", "create " + svnRoot.getAbsolutePath() );
    }

    protected String getFileUrl( File repositoryRootFile )
        throws CommandLineException
    {
        String repositoryRoot = repositoryRootFile.getAbsolutePath();

        // TODO: it'd be great to build this into CommandLineUtils somehow
        // TODO: some way without a custom cygwin sys property?
        if ( "true".equals( System.getProperty( "cygwin" ) ) )
        {
            Commandline cl = new Commandline();

            cl.setExecutable( "cygpath" );

            cl.createArgument().setValue( "--unix" );

            cl.createArgument().setValue( repositoryRoot );

            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

            int exitValue = CommandLineUtils.executeCommandLine( cl, stdout, null );

            if ( exitValue != 0 )
            {
                throw new CommandLineException( "Unable to convert cygwin path, exit code = " + exitValue );
            }

            repositoryRoot = stdout.getOutput().trim();
        }
        else if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            repositoryRoot = "/" + StringUtils.replace( repositoryRoot, "\\", "/" );
        }

        return "file://" + repositoryRoot;
    }

    private String getScmUrl( File repositoryRootFile )
        throws CommandLineException
    {
        return "scm:svn:" + getFileUrl( repositoryRootFile );
    }

    protected String makeScmUrl( String scm, File scmRoot, String artifactId )
        throws CommandLineException
    {
        if ( "cvs".equals( scm ) )
        {
            return "scm|cvs|local|" + scmRoot.getAbsolutePath() + "|" + artifactId;
        }
        else if ( "svn".equals( scm ) )
        {
            return getScmUrl( scmRoot ) + "/" + artifactId;
        }

        throw new RuntimeException( "Unknown SCM type '" + scm + "'" );
    }

    // ----------------------------------------------------------------------
    // Maven 1
    // ----------------------------------------------------------------------

    protected void writeMavenOnePom( File file, String artifactId, String scmUrl, String email )
        throws IOException
    {
        PrintWriter writer = new PrintWriter( new FileWriter( file ) );

        writer.println( "<project>" );
        writer.println( "  <pomVersion>3</pomVersion>" );
        writer.println( "  <groupId>continuum</groupId>" );
        writer.println( "  <artifactId>" + artifactId + "</artifactId>" );
        writer.println( "  <currentVersion>1.0</currentVersion>" );
        writer.println( "  <name>Maven 1 Project</name>" );
        writer.println( "  <repository>" );
        writer.println( "    <connection>" + scmUrl + "</connection>" );
        writer.println( "  </repository>" );
        writer.println( "  <build>" );
        writer.println( "    <nagEmailAddress>" + email + "</nagEmailAddress>" );
        writer.println( "  </build>" );
        writer.println( "</project>" );
        writer.close();
    }
    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    public static void progress( String message )
    {
        System.out.println( "[" + progressDateFormat.format( new Date() ) + "] " + message );
    }

    public static void print( String message )
    {
        System.out.println( "[" + progressDateFormat.format( new Date() ) + "] " + message );
    }
}
