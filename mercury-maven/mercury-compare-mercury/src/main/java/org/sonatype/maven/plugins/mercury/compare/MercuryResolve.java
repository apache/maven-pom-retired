package org.sonatype.maven.plugins.mercury.compare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.compare.DataBuilder;
import org.apache.maven.mercury.compare.DataException;
import org.apache.maven.mercury.compare.IDepResolver;
import org.apache.maven.mercury.compare.mdo.Dependencies;
import org.apache.maven.mercury.compare.mdo.Dependency;
import org.apache.maven.mercury.metadata.DependencyBuilder;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.Util;

/**
 * 
 * @author Oleg Gusakov
 *
 */

public class MercuryResolve
implements IDepResolver
{
  static final String SYSTEM_PROPERTY_LOCAL_REPO="localRepo";
  static final String localRepoDir = System.getProperty( SYSTEM_PROPERTY_LOCAL_REPO, "../localRepoMercury" );
//  static final String localRepoDir = System.getProperty( SYSTEM_PROPERTY_LOCAL_REPO, "../localRepoMaven" );
  static final File   localRepoDirFile = new File( localRepoDir );
  
  static final String SYSTEM_PROPERTY_LIST_FILE="list";
  static final String list = System.getProperty( SYSTEM_PROPERTY_LIST_FILE, "../list.txt" );
  static final File   listFile = new File( list );
  
  static final String SYSTEM_PROPERTY_REPOS_FILE="repoList";
  static final String repoList = System.getProperty( SYSTEM_PROPERTY_REPOS_FILE, "../repositories.properties" );
  static final File   repoListFile = new File( repoList );

  static final String SYSTEM_PROPERTY_TARGET_DIR="targetDir";
  static final String targetDir = System.getProperty( SYSTEM_PROPERTY_TARGET_DIR, "../deps" );
  static final File   targetDirFile = new File( targetDir );
  
  static final List<Repository> repos = new ArrayList<Repository>(8);
  
  static LocalRepositoryM2 lRepo;
  
  static DependencyBuilder depBuilder;
  
	//----------------------------------------------------------------
	public static void main( String [] av )
	throws Exception
	{

    if( !localRepoDirFile.exists() )
      localRepoDirFile.mkdirs();

    if( !listFile.exists() )
      throw new Exception(listFile.getCanonicalPath()+" does nor exist");

    if( !repoListFile.exists() )
      throw new Exception(repoListFile.getCanonicalPath()+" does nor exist");

    if( !targetDirFile.exists() )
      targetDirFile.mkdirs();
    
    lRepo = new LocalRepositoryM2( "localMercury", new File(localRepoDir) );
    repos.add( lRepo );
    
    Properties rep = new Properties();
    rep.load( new FileInputStream(repoListFile) );

    for( Object key : rep.keySet() )
    {
      Server server = new Server( (String)key, new URL(rep.getProperty( (String)key )) );
      RemoteRepositoryM2 repo = new RemoteRepositoryM2( server );
      
      repos.add( repo );
    }
    
    depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, repos, null, null, null );

    MercuryResolve mc = new MercuryResolve();
    
    if( av == null || av.length < 1 )
      DataBuilder.visitDeps( listFile, mc );

	}
	
	//------------------------------------------------------------------------
  public void resolve( String groupId, String artifactId, String version, String type )
  throws Exception
  {
    String ac = groupId+":"+artifactId+":"+version+"::"+type;
    
    System.out.println("\n======>  "+ac);
    
    long start = System.currentTimeMillis();
    
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata( ac );
    
    MetadataTreeNode root = depBuilder.buildTree( bmd, null );
    long ll = System.currentTimeMillis();
    System.out.println("BuildTree: " + (ll - start) );
    
    List<ArtifactMetadata> dl = depBuilder.resolveConflicts( root );
    System.out.println("resolveDeps: " + (System.currentTimeMillis() - ll) );
    
    File df = DataBuilder.getFile( targetDirFile, bmd.getGroupId(), bmd.getArtifactId(), bmd.getVersion(), bmd.getType() );
    
    System.out.println( df.getCanonicalPath() );
    
    Dependencies deps = null;
    
    if( df.exists() )
    {
      deps = DataBuilder.read( df );
      List l = deps.getMercury();
      if( !Util.isEmpty( l ) )
        l.clear();
    }
    else
    {
      deps = new Dependencies();
    
      deps.setGroupId( bmd.getGroupId() );
      deps.setArtifactId( bmd.getArtifactId() );
      deps.setVersion( bmd.getVersion() );
      deps.setType( bmd.getType() );
    }
    
    deps.setMercuryMillis( System.currentTimeMillis() - start );
    
    for( ArtifactMetadata am : dl )
    {
      Dependency dep = new Dependency();
      dep.setGroupId( am.getGroupId() );
      dep.setArtifactId( am.getArtifactId() );
      dep.setVersion( am.getVersion() );
      dep.setType( am.getType() );
      dep.setScope( am.getScope() );
      
      deps.addMercury( dep );
    }
    
    DataBuilder.write( deps, df );
    
  }
	
  private void compare( Collection<ArtifactBasicMetadata> r1, Collection<ArtifactBasicMetadata> r2 )
  {
  }
	//----------------------------------------------------------------
	//----------------------------------------------------------------
}
