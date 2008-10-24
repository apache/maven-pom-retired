package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryUpdateIntervalPolicy;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

public class VirtualRepositoryReaderIntegratedTest
extends TestCase
{
  File _testBase;
  File _localRepoBase;
  
  public String _port;
  HttpTestServer _server;
  
  List<ArtifactBasicMetadata> _query;
  
  RemoteRepositoryM2 _remoteRepo;
  LocalRepositoryM2  _localRepo;
  
  
  VirtualRepositoryReader _vr;

  //-------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    _testBase = new File("./target/test-classes/repoVr");
    _localRepoBase = new File("./target/localRepo");
    
    FileUtil.delete( _localRepoBase );
    _localRepoBase.mkdirs();
    
    _server = new HttpTestServer( _testBase, "/repo" );
    _server.start();
    _port = String.valueOf( _server.getPort() );

    _query = new ArrayList<ArtifactBasicMetadata>();

    Server server = new Server( "testRemoteRepo", new URL("http://localhost:"+_port+"/repo") );
    _remoteRepo = new RemoteRepositoryM2( server );
    _remoteRepo.setUpdatePolicy( new RepositoryUpdateIntervalPolicy("interval2").setInterval( 2000L ) );
    
    _localRepo = new LocalRepositoryM2( "testLocalRepo", _localRepoBase );
    
    List<Repository> reps = new ArrayList<Repository>();
    reps.add( _remoteRepo );
    reps.add( _localRepo );
    
    DependencyProcessor mdProcessor = new MetadataProcessorMock();

    _vr = new VirtualRepositoryReader( reps, mdProcessor );
  }
  //-------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
    _server.stop();
    _server.destroy();
  }
  //-------------------------------------------------------------------------
  public void testReadArtifact()
  throws Exception
  {
    try
    {
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata("a:a:[1,)");
    List<ArtifactBasicMetadata> q = THelper.toList( bmd );
    
    ArtifactBasicResults vres = _vr.readVersions( q );
     
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    List<ArtifactBasicMetadata> versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 5, versions.size() );
    
    // add version 6 to GA metadata
    File mdf = new File( _testBase, "a/a/maven-metadata.xml");
    Metadata md = MetadataBuilder.getMetadata( FileUtil.readRawData( mdf ) );
    
    byte [] newBytes = MetadataBuilder.changeMetadata( md, new AddVersionOperation(new StringOperand("6")) );
    
    FileUtil.writeRawData( mdf, newBytes );
    
    // repository policy is 2 sec, this should be still 5 versions
    vres = _vr.readVersions( q );
    
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 5, versions.size() );

    Thread.sleep( 3000L );
    
    // repository policy is 2 sec, this should cause VR to re-read metadata
    // should now have 6 versions
    vres = _vr.readVersions( q );
    
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 6, versions.size() );
    
    }
    finally
    {
      File mdf = new File( _testBase, "a/a/maven-metadata.xml");
      InputStream in = VirtualRepositoryReaderIntegratedTest.class.getResourceAsStream( "/repoVr/a.a-maven-metadata.xml" );
      FileUtil.writeRawData( mdf, in );
    }
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
