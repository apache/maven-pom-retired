package org.sonatype.maven.plugins.mercury.compare;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.metadata.DependencyBuilder;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.Util;
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
  
  List<Repository> repos;
  
  DependencyBuilder depBuilder;
  
	//----------------------------------------------------------------
	public void execute()
	throws MojoExecutionException, MojoFailureException
	{

    if( _session == null )
      throw new MojoExecutionException("session not injected");

    if( localRepoDir == null )
      throw new MojoExecutionException("local repo dir not injected");

    try {
		  
		  if( projectBuilder == null )
		    throw new Exception("project builder is null");
		  
      ArtifactBasicMetadata query = new ArtifactBasicMetadata("asm:asm-xml:3.0::jar");
      
      Collection<ArtifactBasicMetadata> res1 = getMaven( query );
      
      Collection<ArtifactBasicMetadata> res2 = getMaven( query );
      
      compare( res1, res2 );

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
      
      repos = new ArrayList<Repository>();
      
      LocalRepositoryM2 lRepo = new LocalRepositoryM2( "localMercury", new File(localRepoDir) );
      repos.add( lRepo );
      
      for( ArtifactRepository ar : (List<ArtifactRepository>)remoteRepositories )
      {
        Server server = new Server( ar.getId(), new URL(ar.getUrl()) );
        
        RemoteRepositoryM2 rr = new RemoteRepositoryM2( server );
        
        repos.add( rr );
      }
      
      depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, repos, null, null, null );
      
    }
    catch( Exception e )
    {
      throw new InitializationException(e.getMessage());
    }
  }
  
  private Collection<ArtifactBasicMetadata> getMaven( ArtifactBasicMetadata bmd )
  throws MalformedURLException, ArtifactResolutionException, ArtifactNotFoundException, ProjectBuildingException, InvalidDependencyVersionException
  {
    Set<org.apache.maven.artifact.Artifact> deps = resolver.transitivelyResolvePomDependencies( projectBuilder , "asm", "asm-xml", "3.0", false );
    
    if( deps == null || deps.isEmpty() )
      return null;
    
    ArrayList<ArtifactBasicMetadata> res = new ArrayList<ArtifactBasicMetadata>( deps.size() );
    
    for( org.apache.maven.artifact.Artifact a : deps )
    {
      res.add( 
        new ArtifactBasicMetadata( 
            a.getGroupId()+":"+a.getArtifactId()+":"+a.getVersion()
            + ":"+Util.nvlS( a.getClassifier(), "" )
            + ":"+Util.nvlS( a.getType(), ArtifactBasicMetadata.DEFAULT_ARTIFACT_TYPE )
                                  ) 
             );
    }

    return res;
  }

  private Collection<? extends ArtifactBasicMetadata> getMercury( ArtifactBasicMetadata bmd )
  throws MetadataTreeException
  {
    MetadataTreeNode root = depBuilder.buildTree( bmd, null );
    
    List<ArtifactMetadata> deps = depBuilder.resolveConflicts( root );
    
    return deps;
  }
	
  private void compare( Collection<ArtifactBasicMetadata> r1, Collection<ArtifactBasicMetadata> r2 )
  {

    _log.info("\n-------------------------------> Maven Results:");
    System.out.println( r1 );
    _log.info("\n<-------------------------------");

    _log.info("\n-------------------------------> Mercury Results:");
    System.out.println( r2 );
    _log.info("\n<-------------------------------");

  }
	//----------------------------------------------------------------
	//----------------------------------------------------------------
}
