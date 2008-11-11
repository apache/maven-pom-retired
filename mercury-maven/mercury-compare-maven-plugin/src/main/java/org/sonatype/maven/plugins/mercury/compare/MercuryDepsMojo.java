package org.sonatype.maven.plugins.mercury.compare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.mortbay.jetty.plugin.RuntimeDependencyResolver;
import org.mortbay.jetty.plugin.util.PluginLog;

/**
 * 
 * @author Oleg Gusakov
 *
 *  @goal deps
 *  @requiresProject false
 */

public class MercuryDepsMojo
extends AbstractMojo
implements Initializable
{
  private static Log _log;
	//----------------------------------------------------------------
  /**
   * @parameter expression="${targetDir}"
   */
  String targetDir;
  /**
   * @parameter expression="${listFile}"
   */
  String listFile;
	/**
	  * @parameter expression="${session}"
	  */
	MavenSession _session;
	 /**
   * @component
   */
  private ArtifactResolver artifactResolver;
    /**
   *
   * @component
   */
  private ArtifactFactory artifactFactory;
  /**
  *
  * @component
  */
  private ArtifactMetadataSource metadataSource;
  /**
   *
   * @parameter expression="${localRepository}"
   */
  private ArtifactRepository localRepository;
  /**
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   */
  private List remoteRepositories;
//
//	/**
//	  * @parameter expression="${project}"
//	  */
//	MavenProject _project;
//
//	/**
//	  * @component
//	  */
//	Prompter _prompter;
  
  PlexusContainer plexus;
  
  RuntimeDependencyResolver resolver;
  
  MavenProjectBuilder projectBuilder;
  
  File target;
  
	//----------------------------------------------------------------
	public void execute()
	throws MojoExecutionException, MojoFailureException
	{

    if( _session == null )
      throw new MojoExecutionException("session not injected");

    if( targetDir == null )
      throw new MojoExecutionException("target dir not specified");
    
    target = new File( targetDir );
    
    if( !target.exists() )
    {
      target.mkdirs();
    }
    else
    {
      if( target.isFile() )
        throw new MojoExecutionException("target directory turned to be a file, not a folder :" + targetDir );
    }

    if( listFile == null )
      throw new MojoExecutionException("list file not specified");
    
    File list = new File( listFile );
    
    if( list.exists() )
      throw new MojoExecutionException( "list file "+listFile+" does not exist" );

    try {
      BufferedReader r = new BufferedReader( new FileReader(list) );
      
      for( String line = r.readLine(); line != null; line = r.readLine() )
      {
        StringTokenizer st = new StringTokenizer( line, " :" );
        
        int count = st.countTokens();
        
        if( count < 3 || count > 4 )
        {
          _log.info( "Cannot parse line: "+line );
          continue;
        }
        
        int i = 0;
        
        String [] gav = new String[4];
        
        while( st.hasMoreTokens() )
          gav[i++ ] = st.nextToken();
        
        saveDependencies( gav[0], gav[1], gav[2], count == 4 ? gav[3] : "jar" );
      }
		  
		} catch( Exception e ) {
		  _log.error( e.getMessage() );
			throw new MojoExecutionException( e.getMessage() );
		}
	}
	
	//------------------------------------------------------------------------
  public void initialize()
  throws InitializationException
  {
    _log = getLog();
    
    PluginLog.setLog( _log );
    
    if( _session == null )
      throw new InitializationException( "Maven session is not injected by the container ");
    
    plexus = _session.getContainer();
    
    resolver = new RuntimeDependencyResolver( artifactFactory
                                            , artifactResolver
                                            , metadataSource
                                            , localRepository
                                            , remoteRepositories
                                           );
    
    try
    {
      projectBuilder = (MavenProjectBuilder)plexus.lookup( MavenProjectBuilder.ROLE );
      
      if( projectBuilder == null )
        throw new Exception("project builder is null");
    }
    catch( Exception e )
    {
      throw new InitializationException(e.getMessage());
    }
  }
  
  private void saveDependencies( String groupId, String artifactId, String version, String type )
  throws ArtifactResolutionException, ArtifactNotFoundException, ProjectBuildingException, InvalidDependencyVersionException, IOException
  {
    Set<Artifact> deps = (Set<Artifact>)resolver.transitivelyResolvePomDependencies( projectBuilder , groupId, artifactId, version, false );
    
    File fout = new File( target, groupId+"-"+artifactId+"-"+version+"-"+type+".deps" );
    
    BufferedWriter w = new BufferedWriter( new FileWriter(fout) );
    
    try
    {
      w.write( groupId+":"+artifactId+":"+version+"::"+type );
      
      if( deps == null || deps.isEmpty() )
        return;
    
      for( Artifact a : deps )
      {
        String cl = a.getClassifier();
        
        if( cl == null  )
          cl = "";
        
        w.write( a.getGroupId()+":"+a.getArtifactId()+":"+a.getVersion()+":"+cl+":"+a.getType() );
      }
    }
    finally
    {
      if( w != null )
        try { w.flush(); w.close(); } catch( Exception e ) { _log.error( e.getMessage() ); }
    }
  }
	//----------------------------------------------------------------
	//----------------------------------------------------------------
}
