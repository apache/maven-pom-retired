package org.apache.maven.mercury.dependency.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.metadata.DependencyBuilder;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
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
//  String repoUrl = "http://nexus:8081/nexus/content/groups/public";

  File repoDir;
  
  DependencyBuilder depBuilder;
  LocalRepositoryM2 localRepo;
  RemoteRepositoryM2 remoteRepo;
  List<Repository> reps;
  
  VirtualRepositoryReader vReader;
  
  //----------------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    
    Logger.getLogger("").setLevel(Level.ALL);

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
    
//    Server central = new Server("central", new URL("http://repo1.maven.org/maven2") );
//    RemoteRepositoryM2 centralRepo = new RemoteRepositoryM2(central);
//    reps.add(centralRepo);

    depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, reps, null, null, null );
    
    vReader = new VirtualRepositoryReader( reps );
  }
  //----------------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }
  //----------------------------------------------------------------------------------------------
  public void testDummy()
  throws MetadataTreeException
  {
  }
  //----------------------------------------------------------------------------------------------
  /**
   * this test relies on MavenVersionRange maven.mercury.osgi.version being set to false, it's default value.
   * <strong>Do not</strong> run maven with -Dmaven.mercury.osgi.version=true   
   */
  public void testResolveConflicts()
  throws Exception
  {
//    String artifactId = "org.testng:testng:5.7";
    String artifactId = "asm:asm-xml:3.0";
//  String artifactId = "org.apache.maven:maven-core:2.0.9";
//  String artifactId = "qdox:qdox:1.6.1";
    
    
    ArtifactMetadata md = new ArtifactMetadata( artifactId );

    MetadataTreeNode root = depBuilder.buildTree( md );

    assertNotNull( "null tree built", root );
    
//    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = depBuilder.resolveConflicts( root, ArtifactScopeEnum.compile );
    
    assertNotNull( res );
    
    assertTrue( res.size() > 1 );
    
    System.out.println("\n---------------------------------\nclasspath: "+res);    
    System.out.println("---------------------------------");    
    for( ArtifactMetadata amd : res )
    {
      System.out.println(amd + ( amd.getTracker() == null ? " [no tracker]" : " ["+((RepositoryReader)amd.getTracker()).getRepository().getServer().toString()+"]" ) );
    }
    System.out.println("---------------------------------");    

    
    assertEquals( 4, res.size() );
    
    assertTrue( assertHasArtifact( res, "asm:asm-xml:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-util:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-tree:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm:3.0" ) );
    
    ArtifactResults aRes = vReader.readArtifacts( res );
    
    assertNotNull( aRes );
    
    assertFalse( aRes.hasExceptions() );
    
    assertTrue( aRes.hasResults() );
    
    List<Artifact> artifacts = new ArrayList<Artifact>();
    
    for( ArtifactBasicMetadata abm : aRes.getResults().keySet() )
      artifacts.addAll(  aRes.getResults(abm) );
    
    localRepo.getWriter().writeArtifacts( artifacts );
    
    System.out.println("Saved "+artifacts.size()+" artifacts to "+localRepo.getDirectory() );
    
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