package org.apache.maven.mercury.repository.local.flat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryFlatTest
    extends TestCase
{
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( LocalRepositoryFlatTest.class ); 

  File _dir;
  LocalRepositoryFlat _repo;
  
  String repoUrl = "http://repo1.sonatype.org";
//  String repoUrl = "http://repository.sonatype.org/content/groups/public";
  
  Artifact a;
  Artifact b;

  @Override
  protected void setUp()
      throws Exception
  {
    _dir = File.createTempFile( "test-flat-", "-repo" );
    _dir.delete();
    _dir.mkdirs();
    
    _repo = new LocalRepositoryFlat("testFlatRepo", _dir, false, false );
    
    byte [] pomBlob = "pomblob".getBytes();
    
    a = new DefaultArtifact( new ArtifactBasicMetadata("a:a:1.0.0") );
    
    File ant = File.createTempFile( "test-flat", "-repo" );
    InputStream in = LocalRepositoryFlatTest.class.getResourceAsStream( "/ant-1.6.5.jar" );
    FileUtil.writeRawData( ant, in );
    a.setFile( ant );
    a.setPomBlob( pomBlob );
    
    b = new DefaultArtifact( new ArtifactBasicMetadata("b:b:1.0.0") );
    
    File antlr = File.createTempFile( "test-flat", "-repo" );
    in = LocalRepositoryFlatTest.class.getResourceAsStream( "/antlr-2.7.7.jar" );
    FileUtil.writeRawData( antlr, in );
    b.setFile( antlr );
    b.setPomBlob( pomBlob );
  }
  
  public void testWriteFlat()
  throws Exception
  {
    String test = "testWriteFlat()";
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File bf = new File ( _dir, "b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
  }
  
  
  public void testWriteFlatWithPom()
  throws Exception
  {
    String test = "testWriteFlatWithGroup()";
    
    _repo.setCreatePoms( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File ap = new File ( _dir, "a-1.0.0.pom" );
    assertTrue( ap.exists() );
    
    File bf = new File ( _dir, "b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
    
    File bp = new File ( _dir, "b-1.0.0.pom" );
    assertTrue( bp.exists() );
  }
  
  public void testWriteFlatWithGroup()
  throws Exception
  {
    String test = "testWriteFlatWithGroup()";
    
    _repo.setCreateGroupFolders( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a/a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File bf = new File ( _dir, "b/b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
  }
  
  public void testWriteFlatWithGroupAndPom()
  throws Exception
  {
    String test = "testWriteFlatWithGroup()";
    
    _repo.setCreateGroupFolders( true );
    _repo.setCreatePoms( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a/a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File ap = new File ( _dir, "a/a-1.0.0.pom" );
    assertTrue( ap.exists() );
    
    File bf = new File ( _dir, "b/b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
    
    File bp = new File ( _dir, "b/b-1.0.0.pom" );
    assertTrue( bp.exists() );
  }
  

}
