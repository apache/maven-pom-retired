package org.sonatype.maven.plugins.mercury.compare.maven;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.mercury.compare.DataBuilder;
import org.apache.maven.mercury.compare.IDepResolver;
import org.apache.maven.mercury.compare.mdo.Dependencies;
import org.apache.maven.mercury.compare.mdo.Dependency;
import org.apache.maven.mercury.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
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
implements IDepResolver
{
  private static Log _log;
	//----------------------------------------------------------------
  /**
   * @parameter expression="${localRepo}"
   */
  String localRepo;
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
  * @component
  */
  private MavenProjectBuilder projectBuilder;
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
  
  PlexusContainer plexus;
  
  RuntimeDependencyResolver resolver;
  
  File target;
  
	//----------------------------------------------------------------
	public void execute()
	throws MojoExecutionException, MojoFailureException
	{

    if( _session == null )
      throw new MojoExecutionException("session not injected");

    if( projectBuilder == null )
      throw new MojoExecutionException("project builder is null");
    
    _log = getLog();
    
    PluginLog.setLog( _log );

    resolver = new RuntimeDependencyResolver( artifactFactory
                                            , artifactResolver
                                            , metadataSource
                                            , localRepository
                                            , remoteRepositories
                                           );
    
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
    
    File lr = new File( localRepo );

    localRepository = new DefaultArtifactRepository( "localRepo", lr.toURI().toString(), new DefaultRepositoryLayout() );
    
    _log.info( "local repo set to "+lr.getAbsolutePath() );

    if( listFile == null )
      throw new MojoExecutionException("list file not specified");
    
    File list = new File( listFile );

    try {
      DataBuilder.visitDeps( list, this );
		} catch( Exception e ) {
		  _log.error( e.getMessage() );
			throw new MojoExecutionException( e.getMessage() );
		}

	}
	//-----------------------------------------------------------------------------------------------------------------
  public void resolve( String groupId, String artifactId, String version, String type )
  throws Exception
  {
    
    long start = System.currentTimeMillis();
    
    Set<Artifact> dl = (Set<Artifact>)resolver.transitivelyResolvePomDependencies( projectBuilder , groupId, artifactId, version, false );
    
    File df = DataBuilder.getFile( target, groupId, artifactId, version, type );
    
    System.out.println( df.getCanonicalPath() );
    
    Dependencies deps = null;
    
    if( df.exists() )
    {
      deps = DataBuilder.read( df );
      List l = deps.getMaven();
      if( !Util.isEmpty( l ) )
        l.clear();
    }
    else
    {
      deps = new Dependencies();
    
      deps.setGroupId( groupId );
      deps.setArtifactId( artifactId );
      deps.setVersion( version );
      deps.setType( type );
    }
    
    deps.setMavenMillis( System.currentTimeMillis() - start );
    
    for( Artifact am : dl )
    {
      String sc = am.getScope();
      
      if( "test".equals( sc ) || "provided".equals( sc ) )
        continue;
        
      Dependency dep = new Dependency();
      dep.setGroupId( am.getGroupId() );
      dep.setArtifactId( am.getArtifactId() );
      dep.setVersion( am.getVersion() );
      dep.setType( am.getType() );
      dep.setScope( am.getScope() );
      
      deps.addMaven( dep );
    }
    
    DataBuilder.write( deps, df );

  }
  //-----------------------------------------------------------------------------------------------------------------
  //-----------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.compare.IDepResolver#visit(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public void visit(
      String groupI,
      String artifactId,
      String version,
      String type )
      throws Exception
  {
    // TODO Auto-generated method stub
    
  }
}
