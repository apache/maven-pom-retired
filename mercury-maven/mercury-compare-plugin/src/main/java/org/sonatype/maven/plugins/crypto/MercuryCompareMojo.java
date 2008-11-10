package org.sonatype.maven.plugins.crypto;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.mortbay.jetty.plugin.RuntimeDependencyResolver;
import org.mortbay.jetty.plugin.util.PluginLog;

/**
 * 
 * @author Oleg Gusakov
 *
 *  @goal compare
 *  @requiresProject false
 */

public class MercuryCompareMojo
extends AbstractMojo
implements Initializable
{
  private static Log _log;
	//----------------------------------------------------------------
  /**
   * @parameter expression="${localRepoDir}"
   */
  String localRepoDir;
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
  
	//----------------------------------------------------------------
	public void execute()
	throws MojoExecutionException, MojoFailureException
	{

    if( _session == null )
      throw new MojoExecutionException("session not injected");

//    if( localRepoDir == null )
//      throw new MojoExecutionException("local repo dir not injected");

    try {
		  
		  System.out.println("Got plexus as "+plexus);
      System.out.println("Got resolver as "+resolver);
		  
		  if( projectBuilder == null )
		    throw new Exception("project builder is null");
		  
      System.out.println("Got projectBuilder as "+projectBuilder);
      
//      TreeSet<ArtifactBasicMetadata> res = getOld( new ArtifactCoordinates("asm:asm-xml:3.0::jar") );
//      
//
//_log.info("\n-------------------------------> Results:");
//System.out.println( res );
//_log.info("\n<-------------------------------");

		} catch( Exception e ) {
		  _log.error("Error resolving dependencies "+e.getMessage() );
			throw new MojoExecutionException( e.getMessage() );
		}
	}
	
  public void initialize()
  throws InitializationException
  {
    _log = getLog();
    
    PluginLog.setLog( _log );
    
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
    }
    catch( ComponentLookupException e )
    {
      throw new InitializationException(e.getMessage());
    }
    
  }
  
//  private TreeSet<ArtifactBasic> getOld( ArtifactCoordinates ac )
//  {
//    Set<Artifact> res = resolver.transitivelyResolvePomDependencies( projectBuilder , "asm", "asm-xml", "3.0", false );
//    
//    if( res == null || res.isEmpty() )
//      return null;
//    
//    TreeSet<ArtifactBasicMetadata> 
//  }
	
	//----------------------------------------------------------------
	//----------------------------------------------------------------
}
