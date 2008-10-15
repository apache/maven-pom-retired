package org.apache.maven.mercury.repository.virtual;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VirtualRepositoryReaderTest
extends TestCase
{
  File             _testBase;
  LocalRepository  _localRepo;
  Server           _server;
  RemoteRepository _remoteRepo;
  VirtualRepositoryReader _vr;
  
  String _remoteUrl = "http://people.apache.org/~ogusakov/repos/test";
  String _artifactCoordSn = "org.apache.maven.mercury:mercury-repo-virtual:1.0.0-alpha-2-SNAPSHOT";
  String _artifactCoordLatest = "org.apache.maven.mercury:mercury-repo-virtual:1.0.0-alpha-2-LATEST";
  String _artifactCoordRelease = "org.apache.maven.mercury:mercury-repo-virtual:1.0.0-alpha-2-RELEASE";
  
  @Override
  protected void setUp()
  throws Exception
  {
    _testBase = new File( "./target/repo" );
    _testBase.delete();
    _testBase.mkdirs();
    
    if( !_testBase.exists() || !_testBase.isDirectory() )
      throw new Exception( "cannot create clean folder " + _testBase.getAbsolutePath() );
    
    _localRepo = new LocalRepositoryM2( "localRepo", _testBase );
    
    _server = new Server( "remoteRepo", new URL(_remoteUrl) );
    
    _remoteRepo = new RemoteRepositoryM2( _server.getId(), _server );
    
    List<Repository> rl = new ArrayList<Repository>();
    rl.add( _localRepo );
    rl.add( _remoteRepo );
     
    _vr = new VirtualRepositoryReader( rl, DependencyProcessor.NULL_PROCESSOR );
  }
  
  public void testReadSnapshot()
  throws Exception
  {
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata(_artifactCoordSn);
    List<ArtifactBasicMetadata> q = new ArrayList<ArtifactBasicMetadata>();
    q.add( bmd );
    
    ArtifactResults res = _vr.readArtifacts( q );
    
    assertNotNull( res );
    
    assertFalse( res.hasExceptions() );
    
    assertTrue( res.hasResults() );
    
    assertTrue( res.hasResults( bmd ) );
    
    Map<ArtifactBasicMetadata, List<Artifact>> m = res.getResults();
    
    List<Artifact> al = m.get( bmd );
    
    Artifact a = al.get( 0 );
    
    assertTrue( bmd.getGroupId().equals( a.getGroupId() ) );
    assertTrue( bmd.getArtifactId().equals( a.getArtifactId() ) );
    assertTrue( bmd.getVersion().equals( a.getVersion() ) );
    
    byte [] pomBytes = a.getPomBlob(); 
    
    assertTrue( pomBytes != null );
    
    assertEquals( 795, pomBytes.length );
    
    File ab = a.getFile();
    
    assertNotNull( ab );
    
    assertTrue( ab.exists() );
    
    assertEquals( 6162, ab.length() );
  }
  
  public void testWrite()
  throws Exception
  {
    Artifact a = new DefaultArtifact( new ArtifactBasicMetadata("a:a:1.0:text:txt") );
    File bin = File.createTempFile( "vr-", "-test.txt" );
    FileUtil.writeRawData( bin, "test" );
    a.setFile( bin );
    
    List<Artifact> arts = new ArrayList<Artifact>();
    arts.add( a );
    
    _localRepo.getWriter().writeArtifacts( arts );
    
    File af = new File( _testBase, "a/a/1.0/a-1.0-text.txt");
    
    assertTrue( af.exists() );
    
    assertEquals( 4, af.length() );
    
  }
}
