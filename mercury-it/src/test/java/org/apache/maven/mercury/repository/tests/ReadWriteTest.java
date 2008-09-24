package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ReadWriteTest
extends TestCase
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( ReadWriteTest.class ); 

  File remoteRepoBase = new File("./target/test-classes/repo");
  public String port;
  HttpTestServer httpServer;

  RemoteRepository rr;
  LocalRepository  lr;

  DependencyProcessor mdProcessor;
  RepositoryReader reader;

  File localRepoBase;
  RepositoryWriter writer;

  List<ArtifactBasicMetadata> query;
  
  ArtifactBasicMetadata bmd;
  
  Server server;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
      throws Exception
  {
    httpServer = new HttpTestServer( remoteRepoBase, "/repo" );
    httpServer.start();
    port = String.valueOf( httpServer.getPort() );

    server = new Server( "test", new URL("http://localhost:"+port+"/repo") );
    rr = new RemoteRepositoryM2( "testRepo", server );

    mdProcessor = new MetadataProcessorMock();
    reader = rr.getReader( new MetadataProcessorMock() );
    
    localRepoBase = File.createTempFile( "local", "repo" );
    localRepoBase.delete();
    localRepoBase.mkdir();
    log.info("local repo is in "+localRepoBase);
    
    lr = new LocalRepositoryM2( "lr", localRepoBase );
    writer = lr.getWriter(); 

    query = new ArrayList<ArtifactBasicMetadata>();
  }

  protected void tearDown()
  throws Exception
  {
    super.tearDown();
    httpServer.stop();
    httpServer.destroy();
  }
  
  public void testOneArtifact()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:4");
    query.add( bmd );
    
    ArtifactResults res = reader.readArtifacts( query );
    
    assertTrue( res != null );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    Map< ArtifactBasicMetadata, List<Artifact>> resMap = res.getResults();
    
    assertNotNull( resMap );
    assertFalse( resMap.isEmpty() );
    
    List<Artifact> al = resMap.get( bmd );
    
    assertNotNull( al );
    assertFalse( al.isEmpty() );
    
    Artifact a = al.get( 0 );
    
    writer.writeArtifact( al );
    
    File aBin = new File( localRepoBase, "a/a/4/a-4.jar" );
    assertTrue( aBin.exists() );
    
    File aPom = new File( localRepoBase, "a/a/4/a-4.pom" );
    assertTrue( aPom.exists() );
    
    assertNotNull( a.getPomBlob() );
    assertTrue( a.getPomBlob().length > 10 );
    log.info( a+" - pom length is "+a.getPomBlob().length );
  }
  
  public void testOneArtifactWithClassifier()
  throws IllegalArgumentException, RepositoryException
  {
    ArtifactBasicMetadata bm = new ArtifactBasicMetadata("a:a:4:sources");
    query.add( bm );
    
    ArtifactResults res = reader.readArtifacts( query );
    
    assertTrue( res != null );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    Map< ArtifactBasicMetadata, List<Artifact>> resMap = res.getResults();
    
    assertNotNull( resMap );
    assertFalse( resMap.isEmpty() );
    
    List<Artifact> al = resMap.get( bm );
    
    assertNotNull( al );
    assertFalse( al.isEmpty() );
    
    Artifact a = al.get( 0 );
    
    writer.writeArtifact( al );
    
    File aBin = new File( localRepoBase, "a/a/4/a-4-sources.jar" );
    assertTrue( aBin.exists() );

    log.info( a+" - pom length is "+a.getPomBlob().length );
  }

}
