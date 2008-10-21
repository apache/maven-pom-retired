package org.apache.maven.mercury.dependency.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.metadata.DependencyTreeBuilder;
import org.apache.maven.mercury.metadata.MetadataTreeCircularDependencyException;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DependencyTreeBuilderTest
extends TestCase
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( DependencyTreeBuilderTest.class ); 
  
  String repoUrl = "http://repository.sonatype.org/content/groups/public";

  File repoDir;
  
  DependencyTreeBuilder mt;
  LocalRepositoryM2 localRepo;
  RemoteRepositoryM2 remoteRepo;
  List<Repository> reps;
  DependencyProcessor processor;
  
  //----------------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    repoDir = File.createTempFile( "local-repo-","-it");
    repoDir.delete();
    repoDir.mkdirs();
    
    _log.info( "temporary local repository at "+repoDir );
    
    reps = new ArrayList<Repository>();
    
    localRepo = new LocalRepositoryM2( "testLocalRepo", repoDir );
    reps.add(  localRepo );
    
    Server server = new Server( "testRemoteRepo", new URL(repoUrl) );
    remoteRepo = new RemoteRepositoryM2(server);
    reps.add( remoteRepo );

    processor = new MavenDependencyProcessor();

    mt = new DependencyTreeBuilder( null, null, null, reps, processor );
  }
  //----------------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }
  //----------------------------------------------------------------------------------------------
  public void ntestResolveConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "org.codehaus.plexus:plexus-compiler:1.5.3" );
    
    MetadataTreeNode root = mt.buildTree( md );

    assertNotNull( "null tree built", root );
    
//    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    
    assertNotNull( "null resolution", res );

System.out.println("BigRes: "+res);    
    
//    assertEquals( "wrong tree size", 3, res.size() );
    
//    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
//    assertTrue( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
//    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
  }
  //----------------------------------------------------------------------------------------------
  private static boolean assertHasArtifact( List<ArtifactMetadata> res, String gav )
  {
    ArtifactMetadata gavMd = new ArtifactMetadata(gav);
    
    for( ArtifactMetadata md : res )
      if( md.sameGAV( gavMd ) )
        return true;
    
    return false;
  }
  //----------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------
}
